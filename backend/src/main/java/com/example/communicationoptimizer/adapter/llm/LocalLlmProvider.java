package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import org.springframework.stereotype.Component;

@Component
public class LocalLlmProvider implements LlmProvider {

    @Override
    public String getCode() {
        return "local";
    }

    @Override
    public OptimizeResponse generate(OptimizeRequest request) {
        throw new UnsupportedOperationException("Local LLM provider is not wired yet");
    }
}
