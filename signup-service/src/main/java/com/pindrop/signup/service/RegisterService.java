package com.pindrop.signup.service;

import com.pindrop.signup.dao.UserDdbClient;
import com.pindrop.signup.dto.RegisterUserRequest;
import com.pindrop.signup.dto.RegisterUserResponse;
import com.pindrop.signup.exceptions.SignupServiceSystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserDdbClient userDdbClient;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegisterUserResponse register(RegisterUserRequest request) throws SignupServiceSystemException {
        String now = Instant.now().toString();
        String passwordHash = passwordEncoder.encode(request.getPassword());
        try {
            userDdbClient.putUser(request.getLoginId(), request.getEmail(), passwordHash,
                    String.valueOf(request.getDob()), now);
        } catch (Exception ex) {
            String err = String.format("Failed to register user. loginId=%s. Error=%s", request.getLoginId(), ex.getMessage());
            throw new SignupServiceSystemException(err, ex);
        }

        return RegisterUserResponse.builder()
                .loginId(request.getLoginId())
                .build();
    }
}
