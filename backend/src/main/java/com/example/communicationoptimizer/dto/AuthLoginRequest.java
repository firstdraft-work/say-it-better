package com.example.communicationoptimizer.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {

    @NotBlank
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
