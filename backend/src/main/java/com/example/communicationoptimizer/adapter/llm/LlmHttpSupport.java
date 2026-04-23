package com.example.communicationoptimizer.adapter.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.net.ProxySelector;
import java.net.http.HttpTimeoutException;

@Component
public class LlmHttpSupport {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public LlmHttpSupport(ObjectMapper objectMapper) {
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

    public JsonNode postJson(String url, String bearerToken, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(180))
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("LLM provider request failed: HTTP " + response.statusCode() + " " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (HttpTimeoutException exception) {
            throw new IllegalStateException("LLM provider request timed out", exception);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "LLM provider request failed: " + exception.getClass().getSimpleName() + " " + safeMessage(exception.getMessage()),
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "LLM provider request interrupted: " + safeMessage(exception.getMessage()),
                    exception
            );
        }
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

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "<no-message>" : message;
    }
}
