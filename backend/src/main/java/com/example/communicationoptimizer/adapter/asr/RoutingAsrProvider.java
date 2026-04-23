package com.example.communicationoptimizer.adapter.asr;

import com.example.communicationoptimizer.config.AppProviderProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Primary
@Component
public class RoutingAsrProvider implements AsrProvider {

    private final AppProviderProperties providerProperties;
    private final Map<String, AsrProvider> providers;

    public RoutingAsrProvider(AppProviderProperties providerProperties, List<AsrProvider> providerList) {
        this.providerProperties = providerProperties;
        this.providers = new LinkedHashMap<>();
        for (AsrProvider provider : providerList) {
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
        return providerProperties.getAsr();
    }

    public Set<String> listAvailableCodes() {
        return providers.keySet();
    }

    @Override
    public String transcribe(Long mediaId) {
        AsrProvider provider = providers.get(providerProperties.getAsr());
        if (provider == null) {
            throw new IllegalStateException("Unsupported asr provider: " + providerProperties.getAsr());
        }
        return provider.transcribe(mediaId);
    }
}
