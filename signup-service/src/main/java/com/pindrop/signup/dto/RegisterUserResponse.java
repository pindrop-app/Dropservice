package com.pindrop.signup.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterUserResponse {
    private String loginId;
}
