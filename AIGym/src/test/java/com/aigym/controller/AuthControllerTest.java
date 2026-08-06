package com.aigym.controller;

import com.aigym.dto.authdto.AuthResponseDto;
import com.aigym.dto.authdto.LoginRequestDto;
import com.aigym.dto.authdto.RegisterRequestDto;
import com.aigym.security.RateLimitingService;
import com.aigym.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for pure controller testing
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    void register_Success() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Test User");
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        doNothing().when(rateLimitingService).checkRateLimit(any());
        doNothing().when(authService).register(any(RegisterRequestDto.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công. Vui lòng kiểm tra email để lấy mã OTP."));

        verify(authService, times(1)).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_Fail_InvalidEmail() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Test User");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void login_Success() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access_token"));
    }
}
