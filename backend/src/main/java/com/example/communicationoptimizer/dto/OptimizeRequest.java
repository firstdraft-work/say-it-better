package com.example.communicationoptimizer.dto;

import jakarta.validation.constraints.NotBlank;

public class OptimizeRequest {

    @NotBlank
    private String sourceType;
    private String text;
    private Long mediaId;
    private String scene;
    private String relation;
    private String goal;
    private boolean needTts;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public boolean isNeedTts() {
        return needTts;
    }

    public void setNeedTts(boolean needTts) {
        this.needTts = needTts;
    }
}
