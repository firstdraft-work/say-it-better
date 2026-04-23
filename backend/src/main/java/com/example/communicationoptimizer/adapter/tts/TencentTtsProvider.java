package com.example.communicationoptimizer.adapter.tts;

import com.example.communicationoptimizer.config.AppTtsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class TencentTtsProvider implements TtsProvider {

    private static final String SERVICE = "tts";
    private static final String ACTION = "TextToVoice";
    private static final String VERSION = "2019-08-23";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";

    private final AppTtsProperties ttsProperties;
    private final ObjectMapper objectMapper;
    private final LocalAudioFileStore audioFileStore;
    private final HttpClient httpClient;

    public TencentTtsProvider(AppTtsProperties ttsProperties, ObjectMapper objectMapper, LocalAudioFileStore audioFileStore) {
        this.ttsProperties = ttsProperties;
        this.objectMapper = objectMapper;
        this.audioFileStore = audioFileStore;

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
    public String synthesize(String text) {
        if (isBlank(ttsProperties.getTencent().getSecretId()) || isBlank(ttsProperties.getTencent().getSecretKey())) {
            throw new UnsupportedOperationException("Tencent TTS secretId/secretKey is not configured");
        }

        try {
            String payload = buildPayload(text);
            long timestamp = Instant.now().getEpochSecond();
            String endpoint = ttsProperties.getTencent().getEndpoint();
            String authorization = buildAuthorization(payload, timestamp, endpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + endpoint))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("X-TC-Action", ACTION)
                    .header("X-TC-Version", VERSION)
                    .header("X-TC-Region", ttsProperties.getTencent().getRegion())
                    .header("X-TC-Timestamp", String.valueOf(timestamp))
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Tencent TTS request failed: HTTP " + response.statusCode() + " " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode errorNode = root.path("Response").path("Error");
            if (!errorNode.isMissingNode() && !errorNode.isEmpty()) {
                throw new IllegalStateException("Tencent TTS error: " + errorNode.path("Message").asText("unknown"));
            }

            String audioBase64 = root.path("Response").path("Audio").asText();
            if (audioBase64 == null || audioBase64.isBlank()) {
                throw new IllegalStateException("Tencent TTS returned empty audio");
            }

            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            String extension = ttsProperties.getTencent().getCodec();
            String fileName = audioFileStore.save(audioBytes, extension);
            return "/api/v1/media/audio/" + fileName;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Tencent TTS request failed: " + exception.getClass().getSimpleName() + " " + safeMessage(exception.getMessage()),
                    exception
            );
        }
    }

    private String buildPayload(String text) throws Exception {
        return objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("Text", text)
                .put("SessionId", "tts-" + UUID.randomUUID())
                .put("Volume", ttsProperties.getTencent().getVolume())
                .put("Speed", ttsProperties.getTencent().getSpeed())
                .put("ProjectId", 0)
                .put("ModelType", 1)
                .put("VoiceType", ttsProperties.getTencent().getVoiceType())
                .put("PrimaryLanguage", 1)
                .put("SampleRate", ttsProperties.getTencent().getSampleRate())
                .put("Codec", ttsProperties.getTencent().getCodec())
                .put("EnableSubtitle", false));
    }

    private String buildAuthorization(String payload, long timestamp, String host) throws Exception {
        String date = LocalDate.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC).toString();
        String hashedPayload = sha256Hex(payload);
        String canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + host + "\nx-tc-action:" + ACTION.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedPayload;

        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest);

        byte[] secretDate = hmacSha256(("TC3" + ttsProperties.getTencent().getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, SERVICE);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = HexFormat.of().formatHex(hmacSha256(secretSigning, stringToSign));

        return ALGORITHM + " "
                + "Credential=" + ttsProperties.getTencent().getSecretId() + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signature;
    }

    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "<no-message>" : message;
    }
}
