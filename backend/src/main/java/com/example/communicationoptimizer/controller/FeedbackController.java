package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.common.ApiResponse;
import com.example.communicationoptimizer.dto.FeedbackRequest;
import com.example.communicationoptimizer.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> submit(@Valid @RequestBody FeedbackRequest request) {
        feedbackService.submit(request);
        return ApiResponse.success(Map.of("accepted", true));
    }
}
