package com.example.communicationoptimizer.dto;

public class AuthLoginResponse {

    private String token;
    private UserProfileDto userInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfileDto getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserProfileDto userInfo) {
        this.userInfo = userInfo;
    }
}
