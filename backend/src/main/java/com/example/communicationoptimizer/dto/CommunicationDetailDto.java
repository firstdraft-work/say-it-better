package com.example.communicationoptimizer.dto;

import java.util.List;

public class CommunicationDetailDto {

    private Long recordId;
    private String originalText;
    private AnalysisDto analysis;
    private List<VariantDto> variants;
    private boolean favorite;
    private String createdAt;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public AnalysisDto getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AnalysisDto analysis) {
        this.analysis = analysis;
    }

    public List<VariantDto> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantDto> variants) {
        this.variants = variants;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
