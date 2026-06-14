package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.config.AppJwtProperties;
import com.aigym.domain.entity.RefreshTokenSession;
import com.aigym.domain.entity.User;
import com.aigym.domain.enums.Role;
import com.aigym.dto.authdto.AuthResponseDto;
import com.aigym.dto.authdto.LoginRequestDto;
import com.aigym.dto.authdto.RefreshTokenRequestDto;
import com.aigym.dto.authdto.RegisterRequestDto;
import com.aigym.dto.authdto.VerifyOtpRequestDto;
import com.aigym.repository.RefreshTokenSessionRepository;
import com.aigym.repository.UserRepository;
import com.aigym.security.EmailService;
import com.aigym.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AppJwtProperties jwtProperties;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .email("test@gmail.com")
                .password("encoded_pass")
                .role(Role.USER)
                .isActive(true)
                .build();
        activeUser.setId(1L);

        inactiveUser = User.builder()
                .email("inactive@gmail.com")
                .password("encoded_pass")
                .role(Role.USER)
                .isActive(false)
                .verificationCode("123456")
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5))
                .failedOtpAttempts(0)
                .build();
        inactiveUser.setId(2L);
    }

    // ==========================================
    // REGISTER TESTS
    // ==========================================

    @Test
    void register_Success_CreatesNewUser() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Test User");
        request.setEmail("new@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");

        authService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("new@gmail.com"), anyString());
    }

    @Test
    void register_Fail_EmailAlreadyExistsAndActive() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(activeUser));

        assertThrows(BadRequestException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    // ==========================================
    // VERIFY EMAIL TESTS
    // ==========================================

    @Test
    void verifyEmail_Success() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setEmail("inactive@gmail.com");
        request.setOtp("123456");

        when(userRepository.findByEmail("inactive@gmail.com")).thenReturn(Optional.of(inactiveUser));

        authService.verifyEmail(request);

        assertTrue(inactiveUser.isActive());
        assertNull(inactiveUser.getVerificationCode());
        verify(userRepository, times(1)).save(inactiveUser);
    }

    @Test
    void verifyEmail_Fail_WrongOtp() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setEmail("inactive@gmail.com");
        request.setOtp("000000");

        when(userRepository.findByEmail("inactive@gmail.com")).thenReturn(Optional.of(inactiveUser));

        assertThrows(BadRequestException.class, () -> authService.verifyEmail(request));
        assertEquals(1, inactiveUser.getFailedOtpAttempts());
        verify(userRepository, times(1)).save(inactiveUser);
    }

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    void login_Success() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(activeUser));
        when(jwtProperties.accessExpiration()).thenReturn(3600000L);
        when(jwtProperties.refreshExpiration()).thenReturn(86400000L);
        when(jwtService.generateJti()).thenReturn("jti-123");
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh_token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenSessionRepository, times(1)).save(any(RefreshTokenSession.class));
    }

    @Test
    void login_Fail_UserNotActive() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("inactive@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("inactive@gmail.com")).thenReturn(Optional.of(inactiveUser));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any());
    }

    // ==========================================
    // REFRESH TESTS
    // ==========================================

    @Test
    void refresh_Fail_InvalidToken() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken("invalid_token");

        when(jwtService.isRefreshToken("invalid_token")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.refresh(request));
    }
}
