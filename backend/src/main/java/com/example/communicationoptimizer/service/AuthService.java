package com.example.communicationoptimizer.service;

import com.example.communicationoptimizer.dto.AuthLoginResponse;

public interface AuthService {

    AuthLoginResponse login(String code);

    Long validateToken(String token);
}
