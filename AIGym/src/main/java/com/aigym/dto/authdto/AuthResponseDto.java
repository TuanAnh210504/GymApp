package com.aigym.dto.authdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private Instant accessTokenExpiresAt;
    private Instant refreshTokenExpiresAt;
    private String role;
    private String fullName;
    private String email;
}
