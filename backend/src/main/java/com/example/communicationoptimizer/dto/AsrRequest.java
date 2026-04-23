package com.example.communicationoptimizer.dto;

import jakarta.validation.constraints.NotNull;

public class AsrRequest {

    @NotNull
    private Long mediaId;

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }
}
