package com.pindrop.signup.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private String loginId;
    private String createdAt;
}
