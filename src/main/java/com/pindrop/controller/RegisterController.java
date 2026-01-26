package com.pindrop.controller;

import com.pindrop.dto.RegisterUserRequest;
import com.pindrop.dto.RegisterUserResponse;
import com.pindrop.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/user")
    public ResponseEntity<RegisterUserResponse> registerUser(@Valid @RequestBody RegisterUserRequest req) throws Exception {
        RegisterUserResponse user = registerService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

}
