package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.common.ApiResponse;
import com.example.communicationoptimizer.dto.AsrRequest;
import com.example.communicationoptimizer.dto.AsrResponse;
import com.example.communicationoptimizer.service.SpeechService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/speech")
public class SpeechController {

    private final SpeechService speechService;

    public SpeechController(SpeechService speechService) {
        this.speechService = speechService;
    }

    @PostMapping("/asr")
    public ApiResponse<AsrResponse> asr(@Valid @RequestBody AsrRequest request) {
        return ApiResponse.success(speechService.transcribe(request.getMediaId()));
    }
}
