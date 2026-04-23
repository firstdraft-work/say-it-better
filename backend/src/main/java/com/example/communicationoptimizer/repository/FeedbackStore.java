package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.dto.FeedbackRequest;

public interface FeedbackStore {

    void save(Long userId, FeedbackRequest request);
}
