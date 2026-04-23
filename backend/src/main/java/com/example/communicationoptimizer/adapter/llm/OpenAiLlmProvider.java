package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.config.AppLlmProperties;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class OpenAiLlmProvider implements LlmProvider {

    private final AppLlmProperties llmProperties;
    private final CommunicationPromptFactory promptFactory;
    private final LlmHttpSupport httpSupport;
    private final LlmResultParser resultParser;
    private final ObjectMapper objectMapper;

    public OpenAiLlmProvider(
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
        return "openai";
    }

    @Override
    public OptimizeResponse generate(OptimizeRequest request) {
        if (llmProperties.getOpenai().getApiKey() == null || llmProperties.getOpenai().getApiKey().isBlank()) {
            throw new UnsupportedOperationException("OpenAI API key is not configured");
        }

        JsonNode response = httpSupport.postJson(
                llmProperties.getOpenai().getBaseUrl() + "/v1/responses",
                llmProperties.getOpenai().getApiKey(),
                buildRequestBody(request)
        );

        String rawOutput = extractStructuredOutput(response);
        return resultParser.parse(rawOutput);
    }

    private String buildRequestBody(OptimizeRequest request) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", llmProperties.getOpenai().getModel());

            ArrayNode inputArray = objectMapper.createArrayNode();
            inputArray.add(objectMapper.createObjectNode()
                    .put("role", "system")
                    .put("content", promptFactory.buildSystemPrompt()));
            inputArray.add(objectMapper.createObjectNode()
                    .put("role", "user")
                    .put("content", promptFactory.buildUserPrompt(request)));
            requestBody.set("input", inputArray);

            ObjectNode formatNode = objectMapper.createObjectNode();
            formatNode.put("type", "json_schema");
            formatNode.put("name", "communication_optimization");
            formatNode.put("strict", true);
            formatNode.set("schema", objectMapper.readTree(promptFactory.outputJsonSchema()));

            ObjectNode textNode = objectMapper.createObjectNode();
            textNode.set("format", formatNode);
            requestBody.set("text", textNode);

            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to build OpenAI request body", exception);
        }
    }

    private String extractStructuredOutput(JsonNode responseNode) {
        JsonNode outputArray = responseNode.path("output");
        for (JsonNode outputNode : outputArray) {
            JsonNode contentArray = outputNode.path("content");
            for (JsonNode contentNode : contentArray) {
                if (contentNode.has("text")) {
                    return contentNode.path("text").asText();
                }
            }
        }
        throw new IllegalStateException("OpenAI response did not contain structured text output");
    }
}
