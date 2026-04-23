package com.example.communicationoptimizer.service.impl;

import com.example.communicationoptimizer.dto.FeedbackRequest;
import com.example.communicationoptimizer.repository.FeedbackStore;
import com.example.communicationoptimizer.service.FeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackStore feedbackStore;

    public FeedbackServiceImpl(FeedbackStore feedbackStore) {
        this.feedbackStore = feedbackStore;
    }

    @Override
    public void submit(FeedbackRequest request) {
        feedbackStore.save(request);
    }
}
