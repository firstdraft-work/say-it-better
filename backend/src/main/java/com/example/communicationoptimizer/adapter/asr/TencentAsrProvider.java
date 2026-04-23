package com.example.communicationoptimizer.adapter.asr;

import com.example.communicationoptimizer.config.AppAsrProperties;
import com.example.communicationoptimizer.repository.MediaStore;
import com.example.communicationoptimizer.repository.StoredMedia;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.security.MessageDigest;

@Component
public class TencentAsrProvider implements AsrProvider {

    private static final String SERVICE = "asr";
    private static final String ACTION = "SentenceRecognition";
    private static final String VERSION = "2019-06-14";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";

    private final AppAsrProperties asrProperties;
    private final MediaStore mediaStore;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TencentAsrProvider(AppAsrProperties asrProperties, MediaStore mediaStore, ObjectMapper objectMapper) {
        this.asrProperties = asrProperties;
        this.mediaStore = mediaStore;
        this.objectMapper = objectMapper;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1);

        ProxySelector proxySelector = buildProxySelector();
        if (proxySelector != null) {
            builder.proxy(proxySelector);
        }

        this.httpClient = builder.build();
    }

    @Override
    public String getCode() {
        return "tencent";
    }

    @Override
    public String transcribe(Long mediaId) {
        if (isBlank(asrProperties.getTencent().getSecretId()) || isBlank(asrProperties.getTencent().getSecretKey())) {
            throw new UnsupportedOperationException("Tencent ASR secretId/secretKey is not configured");
        }

        StoredMedia media = mediaStore.get(mediaId);
        if (isBlank(media.getLocalFilePath())) {
            throw new IllegalStateException("Tencent ASR requires a local audio file path");
        }

        try {
            byte[] audioBytes = Files.readAllBytes(Path.of(media.getLocalFilePath()));
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            String voiceFormat = detectVoiceFormat(media.getFileName());
            String payload = buildPayload(base64Audio, audioBytes.length, voiceFormat);

            long timestamp = Instant.now().getEpochSecond();
            String authorization = buildAuthorization(payload, timestamp, asrProperties.getTencent().getEndpoint());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + asrProperties.getTencent().getEndpoint()))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("X-TC-Action", ACTION)
                    .header("X-TC-Version", VERSION)
                    .header("X-TC-Region", asrProperties.getTencent().getRegion())
                    .header("X-TC-Timestamp", String.valueOf(timestamp))
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Tencent ASR request failed: HTTP " + response.statusCode() + " " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode errorNode = root.path("Response").path("Error");
            if (!errorNode.isMissingNode() && !errorNode.isEmpty()) {
                throw new IllegalStateException("Tencent ASR error: " + errorNode.path("Message").asText("unknown"));
            }

            String result = root.path("Response").path("Result").asText();
            if (result == null || result.isBlank()) {
                throw new IllegalStateException("Tencent ASR returned empty result");
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read local audio file for Tencent ASR", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Tencent ASR request interrupted", exception);
        }
    }

    private String buildPayload(String base64Audio, int dataLength, String voiceFormat) {
        try {
            return objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("ProjectId", 0)
                    .put("SubServiceType", 2)
                    .put("EngSerViceType", asrProperties.getTencent().getEngineType())
                    .put("SourceType", 1)
                    .put("VoiceFormat", voiceFormat)
                    .put("Data", base64Audio)
                    .put("DataLen", dataLength));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to build Tencent ASR payload", exception);
        }
    }

    private String buildAuthorization(String payload, long timestamp, String host) {
        try {
            String date = LocalDate.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC).toString();
            String hashedPayload = sha256Hex(payload);
            String canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + host + "\nx-tc-action:" + ACTION.toLowerCase() + "\n";
            String signedHeaders = "content-type;host;x-tc-action";
            String canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedPayload;

            String credentialScope = date + "/" + SERVICE + "/tc3_request";
            String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest);

            byte[] secretDate = hmacSha256(("TC3" + asrProperties.getTencent().getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, SERVICE);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = HexFormat.of().formatHex(hmacSha256(secretSigning, stringToSign));

            return ALGORITHM + " "
                    + "Credential=" + asrProperties.getTencent().getSecretId() + "/" + credentialScope + ", "
                    + "SignedHeaders=" + signedHeaders + ", "
                    + "Signature=" + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build Tencent ASR authorization", exception);
        }
    }

    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash Tencent ASR request", exception);
        }
    }

    private String detectVoiceFormat(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".wav")) {
            return "wav";
        }
        if (lower.endsWith(".m4a")) {
            return "m4a";
        }
        if (lower.endsWith(".aac")) {
            return "aac";
        }
        if (lower.endsWith(".flac")) {
            return "flac";
        }
        return "mp3";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ProxySelector buildProxySelector() {
        String proxyUrl = firstNonBlank(
                System.getenv("HTTPS_PROXY"),
                System.getenv("https_proxy"),
                System.getenv("HTTP_PROXY"),
                System.getenv("http_proxy")
        );

        if (proxyUrl == null) {
            return null;
        }

        try {
            URI proxyUri = URI.create(proxyUrl);
            if (proxyUri.getHost() == null || proxyUri.getPort() == -1) {
                return null;
            }
            return ProxySelector.of(new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
