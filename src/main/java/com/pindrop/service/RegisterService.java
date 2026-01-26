package com.pindrop.service;

import com.pindrop.dao.DDBClient;
import com.pindrop.dto.RegisterUserRequest;
import com.pindrop.dto.RegisterUserResponse;
import com.pindrop.exceptions.DropServiceSystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class RegisterService {

    private final DDBClient ddbClient;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegisterUserResponse register(RegisterUserRequest request) throws Exception {

        String now = Instant.now().toString();
        String passwordHash = passwordEncoder.encode(request.getPassword());

        try {
            ddbClient.putUser(request.getLoginId(), request.getEmail(), passwordHash,
                    String.valueOf(request.getDob()), now);
            // TODO: emit success metric
        } catch (Exception ex) {
            // TODO: emit fail metric + log the error message
            String err = String.format("Failed to Register User. Request: %s. For Exception Message: %s", request,
                    ex.getMessage());
            throw new DropServiceSystemException(err, ex);
        }

        return RegisterUserResponse.builder()
                .loginId(request.getLoginId())
                .build();
    }

}
