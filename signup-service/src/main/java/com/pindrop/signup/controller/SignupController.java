package com.pindrop.signup.controller;

import com.pindrop.signup.dto.SignupRequest;
import com.pindrop.signup.dto.SignupResponse;
import com.pindrop.signup.service.SignupService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping({ "/signup" })
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        try {
            SignupResponse response = signupService.register(req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("signup failed userId={}", req.getLoginId(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Signup failed"));
        }
    }
}
