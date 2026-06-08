package com.aigym.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record AppJwtProperties(
        @NotBlank String secret,
        @NotBlank String issuer,
        @Min(1) long accessExpiration,
        @Min(1) long refreshExpiration
) {
}
