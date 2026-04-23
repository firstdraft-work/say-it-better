package com.example.communicationoptimizer.service.impl;

import com.example.communicationoptimizer.config.WechatProperties;
import com.example.communicationoptimizer.dto.AuthLoginResponse;
import com.example.communicationoptimizer.dto.UserProfileDto;
import com.example.communicationoptimizer.repository.MysqlConnectionFactory;
import com.example.communicationoptimizer.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.storage", name = "mode", havingValue = "mysql")
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private final WechatProperties wechatProperties;
    private final MysqlConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AuthServiceImpl(WechatProperties wechatProperties, MysqlConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        this.wechatProperties = wechatProperties;
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Override
    public AuthLoginResponse login(String code) {
        String openid = callWechatCode2Session(code);
        Long userId = findOrCreateUser(openid);
        String token = createSession(userId);

        AuthLoginResponse response = new AuthLoginResponse();
        response.setToken(token);

        UserProfileDto user = new UserProfileDto();
        user.setId(userId);
        response.setUserInfo(user);
        return response;
    }

    @Override
    public Long validateToken(String token) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id FROM user_session WHERE token = ? AND expires_at > NOW()")) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("user_id");
            }
            return null;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return null;
        }
    }

    private String callWechatCode2Session(String code) {
        String url = String.format(CODE2SESSION_URL, wechatProperties.getAppId(), wechatProperties.getAppSecret(), code);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode json = objectMapper.readTree(response.body());

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new IllegalStateException("WeChat login failed: " + json.get("errmsg").asText());
            }

            return json.get("openid").asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("WeChat code2session request failed: " + e.getMessage(), e);
        }
    }

    private Long findOrCreateUser(String openid) {
        try (Connection conn = connectionFactory.getConnection()) {
            Long userId = findUserByOpenid(conn, openid);
            if (userId != null) {
                return userId;
            }
            return createUser(conn, openid);
        } catch (Exception e) {
            throw new IllegalStateException("User lookup failed: " + e.getMessage(), e);
        }
    }

    private Long findUserByOpenid(Connection conn, String openid) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM app_user WHERE open_id = ?")) {
            stmt.setString(1, openid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
            return null;
        }
    }

    private Long createUser(Connection conn, String openid) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO app_user (open_id, nickname) VALUES (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, openid);
            stmt.setString(2, "用户" + openid.substring(0, Math.min(6, openid.length())));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new IllegalStateException("Failed to create user");
        }
    }

    private String createSession(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user_session (token, user_id, expires_at) VALUES (?, ?, ?)")) {
            stmt.setString(1, token);
            stmt.setLong(2, userId);
            stmt.setString(3, expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Session creation failed: " + e.getMessage(), e);
        }
        return token;
    }
}
