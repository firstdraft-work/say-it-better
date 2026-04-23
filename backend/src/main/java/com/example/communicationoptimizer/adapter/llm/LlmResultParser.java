package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.VariantDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LlmResultParser {

    private final ObjectMapper objectMapper;

    public LlmResultParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OptimizeResponse parse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            AnalysisDto analysis = new AnalysisDto();
            JsonNode analysisNode = root.path("analysis");
            analysis.setScene(analysisNode.path("scene").asText("unknown"));
            analysis.setRelation(analysisNode.path("relation").asText("other"));
            analysis.setGoal(analysisNode.path("goal").asText("other"));
            analysis.setToneTags(readStringArray(analysisNode.path("toneTags")));
            analysis.setRiskPoints(readStringArray(analysisNode.path("riskPoints")));
            analysis.setEmotionLevel(analysisNode.path("emotionLevel").asInt(1));

            List<VariantDto> variants = new ArrayList<>();
            for (JsonNode variantNode : root.path("variants")) {
                VariantDto variant = new VariantDto();
                variant.setType(variantNode.path("type").asText());
                variant.setTitle(variantNode.path("title").asText());
                variant.setText(variantNode.path("text").asText());
                variants.add(variant);
            }

            OptimizeResponse response = new OptimizeResponse();
            response.setAnalysis(analysis);
            response.setVariants(variants);
            return response;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse LLM JSON output", exception);
        }
    }

    private List<String> readStringArray(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        if (arrayNode == null || !arrayNode.isArray()) {
            return values;
        }
        for (JsonNode node : arrayNode) {
            values.add(node.asText());
        }
        return values;
    }
}
