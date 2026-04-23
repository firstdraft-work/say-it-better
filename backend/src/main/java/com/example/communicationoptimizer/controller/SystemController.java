package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.adapter.asr.RoutingAsrProvider;
import com.example.communicationoptimizer.adapter.llm.RoutingLlmProvider;
import com.example.communicationoptimizer.adapter.tts.RoutingTtsProvider;
import com.example.communicationoptimizer.common.ApiResponse;
import com.example.communicationoptimizer.config.AppStorageProperties;
import com.example.communicationoptimizer.dto.ProviderInfoDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final RoutingLlmProvider llmProvider;
    private final RoutingAsrProvider asrProvider;
    private final RoutingTtsProvider ttsProvider;
    private final AppStorageProperties storageProperties;

    public SystemController(
            RoutingLlmProvider llmProvider,
            RoutingAsrProvider asrProvider,
            RoutingTtsProvider ttsProvider,
            AppStorageProperties storageProperties
    ) {
        this.llmProvider = llmProvider;
        this.asrProvider = asrProvider;
        this.ttsProvider = ttsProvider;
        this.storageProperties = storageProperties;
    }

    @GetMapping("/providers")
    public ApiResponse<ProviderInfoDto> getProviders() {
        ProviderInfoDto dto = new ProviderInfoDto();
        dto.setStorageMode(storageProperties.getMode());
        dto.setSelectedLlm(llmProvider.getSelectedCode());
        dto.setSelectedAsr(asrProvider.getSelectedCode());
        dto.setSelectedTts(ttsProvider.getSelectedCode());
        dto.setAvailableLlms(new ArrayList<>(llmProvider.listAvailableCodes()));
        dto.setAvailableAsrs(new ArrayList<>(asrProvider.listAvailableCodes()));
        dto.setAvailableTts(new ArrayList<>(ttsProvider.listAvailableCodes()));
        return ApiResponse.success(dto);
    }
}
