package com.aigym.controller;

import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;
import com.aigym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    void updateMyInfo_Success() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Updated Name");

        UserResponse mockResponse = new UserResponse();
        mockResponse.setFullName("Updated Name");

        when(userService.updateMyInfo(any(UpdateUserRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"));

        verify(userService, times(1)).updateMyInfo(any(UpdateUserRequest.class));
    }

    @Test
    void updateMyInfo_Fail_InvalidRequest() throws Exception {
        // Assuming @Valid requires fullName to be not blank
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateMyInfo(any());
    }
}
