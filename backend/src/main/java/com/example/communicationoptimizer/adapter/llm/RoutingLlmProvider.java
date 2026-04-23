package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.config.AppProviderProperties;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Primary
@Component
public class RoutingLlmProvider implements LlmProvider {

    private final AppProviderProperties providerProperties;
    private final Map<String, LlmProvider> providers;

    public RoutingLlmProvider(AppProviderProperties providerProperties, List<LlmProvider> providerList) {
        this.providerProperties = providerProperties;
        this.providers = new LinkedHashMap<>();
        for (LlmProvider provider : providerList) {
            if (!"router".equals(provider.getCode())) {
                this.providers.put(provider.getCode(), provider);
            }
        }
    }

    @Override
    public String getCode() {
        return "router";
    }

    public String getSelectedCode() {
        return providerProperties.getLlm();
    }

    public Set<String> listAvailableCodes() {
        return providers.keySet();
    }

    @Override
    public OptimizeResponse generate(OptimizeRequest request) {
        return getSelectedProvider().generate(request);
    }

    private LlmProvider getSelectedProvider() {
        LlmProvider provider = providers.get(providerProperties.getLlm());
        if (provider == null) {
            throw new IllegalStateException("Unsupported llm provider: " + providerProperties.getLlm());
        }
        return provider;
    }
}
