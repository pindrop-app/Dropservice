package com.pindrop.signup.service;

import com.pindrop.signup.dao.UserDdbClient;
import com.pindrop.signup.dto.SignupRequest;
import com.pindrop.signup.dto.SignupResponse;
import com.pindrop.signup.exceptions.SignupServiceSystemException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SignupService {

    private static final Logger log = LoggerFactory.getLogger(SignupService.class);

    private final UserDdbClient userDdbClient;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SignupResponse register(SignupRequest request) throws SignupServiceSystemException {
        String now = Instant.now().toString();
        String passwordHash = passwordEncoder.encode(request.getPassword());
        log.info("Registering user. loginId={}", request.getLoginId());
        try {
            userDdbClient.putUser(request.getLoginId(), request.getEmail(), passwordHash,
                    String.valueOf(request.getDob()), now);
        } catch (Exception ex) {
            String err = String.format("Failed to register user. loginId=%s. Error=%s", request.getLoginId(),
                    ex.getMessage());
            log.error(err, ex);
            throw new SignupServiceSystemException(err, ex);
        }

        log.info("User registered successfully. loginId={}", request.getLoginId());
        return SignupResponse.builder()
                .loginId(request.getLoginId())
                .createdAt(now)
                .build();
    }
}
