package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.common.ApiResponse;
import com.example.communicationoptimizer.dto.CommunicationDetailDto;
import com.example.communicationoptimizer.dto.FavoriteRequest;
import com.example.communicationoptimizer.dto.HistoryItemDto;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.TtsRequest;
import com.example.communicationoptimizer.dto.TtsResponse;
import com.example.communicationoptimizer.service.CommunicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/api/v1/communications")
public class CommunicationController {

    private final CommunicationService communicationService;

    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @PostMapping("/optimize")
    public ApiResponse<OptimizeResponse> optimize(@Valid @RequestBody OptimizeRequest request) {
        return ApiResponse.success(communicationService.optimize(request));
    }

    @GetMapping("/optimize")
    public ApiResponse<Void> optimizeGet() {
        throw new UnsupportedOperationException("POST method required for optimize");
    }

    @GetMapping
    public ApiResponse<List<HistoryItemDto>> listHistory() {
        return ApiResponse.success(communicationService.listHistory());
    }

    @GetMapping("/{recordId}")
    public ApiResponse<CommunicationDetailDto> getDetail(@PathVariable Long recordId) {
        return ApiResponse.success(communicationService.getDetail(recordId));
    }

    @PostMapping("/{recordId}/tts")
    public ApiResponse<TtsResponse> synthesize(
            @PathVariable Long recordId,
            @Valid @RequestBody TtsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        TtsResponse response = communicationService.synthesize(recordId, null, request.getText());
        if (response.getAudioUrl() != null && response.getAudioUrl().startsWith("/")) {
            String base = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort();
            response.setAudioUrl(base + response.getAudioUrl());
        }
        return ApiResponse.success(response);
    }

    @PatchMapping("/{recordId}/favorite")
    public ApiResponse<HistoryItemDto> favorite(@PathVariable Long recordId, @RequestBody FavoriteRequest request) {
        return ApiResponse.success(communicationService.updateFavorite(recordId, request.isFavorite()));
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> delete(@PathVariable Long recordId) {
        communicationService.deleteRecord(recordId);
        return ApiResponse.success(null);
    }
}
