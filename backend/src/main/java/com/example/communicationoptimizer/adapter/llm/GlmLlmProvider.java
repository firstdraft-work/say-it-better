package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.config.AppLlmProperties;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class GlmLlmProvider implements LlmProvider {

    private final AppLlmProperties llmProperties;
    private final CommunicationPromptFactory promptFactory;
    private final LlmHttpSupport httpSupport;
    private final LlmResultParser resultParser;
    private final ObjectMapper objectMapper;

    public GlmLlmProvider(
            AppLlmProperties llmProperties,
            CommunicationPromptFactory promptFactory,
            LlmHttpSupport httpSupport,
            LlmResultParser resultParser,
            ObjectMapper objectMapper
    ) {
        this.llmProperties = llmProperties;
        this.promptFactory = promptFactory;
        this.httpSupport = httpSupport;
        this.resultParser = resultParser;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getCode() {
        return "glm";
    }

    @Override
    public OptimizeResponse generate(OptimizeRequest request) {
        if (llmProperties.getGlm().getApiKey() == null || llmProperties.getGlm().getApiKey().isBlank()) {
            throw new UnsupportedOperationException("GLM API key is not configured");
        }

        JsonNode response = httpSupport.postJson(
                buildEndpointUrl(),
                llmProperties.getGlm().getApiKey(),
                buildRequestBody(request)
        );

        String content = response.path("choices").path(0).path("message").path("content").asText();
        return resultParser.parse(content);
    }

    private String buildRequestBody(OptimizeRequest request) {
        try {
            String userPrompt = promptFactory.buildUserPrompt(request)
                    + "\n\n请严格输出 JSON，不要输出 markdown，不要输出解释。";

            return objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("model", llmProperties.getGlm().getModel())
                    .put("temperature", 0.6)
                    .put("stream", false)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "system")
                                    .put("content", promptFactory.buildSystemPrompt()))
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", userPrompt))));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to build GLM request body", exception);
        }
    }

    private String buildEndpointUrl() {
        String baseUrl = llmProperties.getGlm().getBaseUrl();
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/api/paas/v4") || normalized.endsWith("/api/coding/paas/v4") || normalized.endsWith("/v4")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/api/paas/v4/chat/completions";
    }
}
