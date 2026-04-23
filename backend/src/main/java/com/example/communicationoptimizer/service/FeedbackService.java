package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.FeedbackRequest;

public interface FeedbackService {

    void submit(FeedbackRequest request);
}
