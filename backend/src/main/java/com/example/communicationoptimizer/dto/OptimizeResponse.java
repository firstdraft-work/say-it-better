package com.example.communicationoptimizer.dto;

import java.util.List;

public class OptimizeResponse {

    private Long recordId;
    private AnalysisDto analysis;
    private List<VariantDto> variants;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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
}
