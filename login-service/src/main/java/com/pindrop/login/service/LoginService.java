package com.pindrop.login.service;

import com.pindrop.login.dao.UserDdbClient;
import com.pindrop.login.dto.LoginRequest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserDdbClient userDdbClient;

    public LoginService(UserDdbClient userDdbClient) {
        this.userDdbClient = userDdbClient;
    }

    // ✅ Returns String token to match your LoginController
    public String login(LoginRequest req) {

        // Your UI sends login id (email). The DTO method name might be getLoginId().
        String loginId = safe(getLoginId(req));
        String password = safe(req.getPassword());

        log.info("login attempt userId={}", loginId);

        if (loginId.isBlank() || password.isBlank()) {
            return null; // controller can return 400/401
        }

        Map<String, AttributeValue> item = userDdbClient.getUser(loginId);
        if (item == null || item.isEmpty()) {
            log.warn("login failed: user not found loginId={}", loginId);
            return null;
        }

        String storedPassword = firstString(item, "Password", "password");
        if (storedPassword.isBlank()) {
            log.warn("login failed: stored password missing loginId={}", loginId);
            return null;
        }

        // NOTE: dev mode plain compare. If you use BCrypt, swap this.
        if (!storedPassword.equals(password)) {
            log.warn("login failed: password mismatch loginId={}", loginId);
            return null;
        }

        // Simple token placeholder. Replace with JWT later.
        String token = "OK_" + System.currentTimeMillis();
        log.info("login success loginId={}", loginId);
        return token;
    }

    // Supports either getLoginId() or getUserId() depending on your DTO
    private String getLoginId(LoginRequest req) {
        try {
            // Most likely in your project
            return (String) req.getClass().getMethod("getLoginId").invoke(req);
        } catch (Exception ignored) {
            try {
                // fallback
                return (String) req.getClass().getMethod("getUserId").invoke(req);
            } catch (Exception ignored2) {
                return "";
            }
        }
    }

    private String firstString(Map<String, AttributeValue> item, String... keys) {
        for (String k : keys) {
            AttributeValue v = item.get(k);
            if (v != null && v.s() != null) return v.s();
        }
        return "";
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
