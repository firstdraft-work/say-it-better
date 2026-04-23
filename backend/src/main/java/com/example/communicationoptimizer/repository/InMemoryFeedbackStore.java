package com.example.communicationoptimizer.repository;

import com.example.communicationoptimizer.dto.FeedbackRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryFeedbackStore implements FeedbackStore {

    private final List<StoredFeedback> feedbackList = new CopyOnWriteArrayList<>();

    @Override
    public void save(FeedbackRequest request) {
        StoredFeedback feedback = new StoredFeedback();
        feedback.recordId = request.getRecordId();
        feedback.actionType = request.getActionType();
        feedback.variantType = request.getVariantType();
        feedback.score = request.getScore();
        feedback.commentText = request.getCommentText();
        feedback.createdAt = LocalDateTime.now();
        feedbackList.add(feedback);
    }

    public List<StoredFeedback> listAll() {
        return new ArrayList<>(feedbackList);
    }

    public static class StoredFeedback {
        private Long recordId;
        private String actionType;
        private String variantType;
        private Integer score;
        private String commentText;
        private LocalDateTime createdAt;

        public Long getRecordId() {
            return recordId;
        }

        public String getActionType() {
            return actionType;
        }

        public String getVariantType() {
            return variantType;
        }

        public Integer getScore() {
            return score;
        }

        public String getCommentText() {
            return commentText;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
