package com.example.communicationoptimizer.dto;

import jakarta.validation.constraints.NotBlank;

public class MediaUploadRequest {

    @NotBlank
    private String fileName;

    private String source;
    private int durationMs;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }
}
