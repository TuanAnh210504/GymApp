package com.aigym.service;

import com.aigym.dto.authdto.AuthResponseDto;
import com.aigym.dto.authdto.LoginRequestDto;
import com.aigym.dto.authdto.RegisterRequestDto;
import com.aigym.dto.authdto.RefreshTokenRequestDto;
import com.aigym.dto.authdto.VerifyOtpRequestDto;
import com.aigym.dto.authdto.ForgotPasswordRequestDto;
import com.aigym.dto.authdto.ResetPasswordRequestDto;

public interface AuthService {
    void register(RegisterRequestDto request);
    void verifyEmail(VerifyOtpRequestDto request);
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto refresh(RefreshTokenRequestDto request);
    void forgotPassword(ForgotPasswordRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
}
