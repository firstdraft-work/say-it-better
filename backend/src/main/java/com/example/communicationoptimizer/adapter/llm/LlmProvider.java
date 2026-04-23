package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;

public interface LlmProvider {

    String getCode();

    OptimizeResponse generate(OptimizeRequest request);
}
