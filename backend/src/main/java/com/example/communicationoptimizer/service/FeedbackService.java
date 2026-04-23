package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.FeedbackRequest;

public interface FeedbackService {

    void submit(Long userId, FeedbackRequest request);
}
