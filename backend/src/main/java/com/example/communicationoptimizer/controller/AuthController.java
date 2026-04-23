package com.example.communicationoptimizer.controller;

import com.example.communicationoptimizer.common.ApiResponse;
import com.example.communicationoptimizer.dto.AuthLoginRequest;
import com.example.communicationoptimizer.dto.AuthLoginResponse;
import com.example.communicationoptimizer.dto.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/wx/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthLoginResponse response = new AuthLoginResponse();
        response.setToken("mock-token-" + request.getCode());

        UserProfileDto userInfo = new UserProfileDto();
        userInfo.setId(1L);
        userInfo.setNickname("演示用户");
        userInfo.setAvatarUrl("https://example.com/avatar.png");
        response.setUserInfo(userInfo);

        return ApiResponse.success(response);
    }
}
