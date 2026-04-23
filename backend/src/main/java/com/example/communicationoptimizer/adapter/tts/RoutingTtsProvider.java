package com.example.communicationoptimizer.adapter.tts;

import com.example.communicationoptimizer.config.AppProviderProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Primary
@Component
public class RoutingTtsProvider implements TtsProvider {

    private final AppProviderProperties providerProperties;
    private final Map<String, TtsProvider> providers;

    public RoutingTtsProvider(AppProviderProperties providerProperties, List<TtsProvider> providerList) {
        this.providerProperties = providerProperties;
        this.providers = new LinkedHashMap<>();
        for (TtsProvider provider : providerList) {
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
        return providerProperties.getTts();
    }

    public Set<String> listAvailableCodes() {
        return providers.keySet();
    }

    @Override
    public String synthesize(String text) {
        TtsProvider provider = providers.get(providerProperties.getTts());
        if (provider == null) {
            throw new IllegalStateException("Unsupported tts provider: " + providerProperties.getTts());
        }
        return provider.synthesize(text);
    }
}
