package com.example.communicationoptimizer.dto;

import java.util.List;

public class AnalysisDto {

    private String scene;
    private String relation;
    private String goal;
    private List<String> toneTags;
    private List<String> riskPoints;
    private int emotionLevel;

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

    public List<String> getToneTags() {
        return toneTags;
    }

    public void setToneTags(List<String> toneTags) {
        this.toneTags = toneTags;
    }

    public List<String> getRiskPoints() {
        return riskPoints;
    }

    public void setRiskPoints(List<String> riskPoints) {
        this.riskPoints = riskPoints;
    }

    public int getEmotionLevel() {
        return emotionLevel;
    }

    public void setEmotionLevel(int emotionLevel) {
        this.emotionLevel = emotionLevel;
    }
}
