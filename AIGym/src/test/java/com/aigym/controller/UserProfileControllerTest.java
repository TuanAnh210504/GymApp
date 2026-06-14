package com.aigym.controller;

import com.aigym.dto.UserProfile.HealthMetricsResponse;
import com.aigym.dto.UserProfile.UserProfileRequest;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    @WithMockUser
    void getMyProfile_Success() throws Exception {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(1L);
        response.setWeight(70.0);

        when(userProfileService.getMyProfile()).thenReturn(response);

        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weight").value(70.0));
    }

    @Test
    @WithMockUser
    void createOrUpdateMyProfile_Success() throws Exception {
        String jsonPayload = """
            {
              "weight": 70.0,
              "targetWeight": 70.0,
              "height": 175.0,
              "dateOfBirth": "1995-01-01",
              "gender": "MALE",
              "activityLevel": "LIGHTLY_ACTIVE",
              "goalType": "MAINTAIN_WEIGHT"
            }
        """;

        UserProfileResponse response = new UserProfileResponse();
        response.setId(1L);
        response.setWeight(70.0);

        when(userProfileService.createOrUpdateMyProfile(any(UserProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/profiles/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lưu hồ sơ thành công"))
                .andExpect(jsonPath("$.data.weight").value(70.0));
    }

    @Test
    @WithMockUser
    void getMyHealthMetrics_Success() throws Exception {
        HealthMetricsResponse response = HealthMetricsResponse.builder()
                .bmi(22.8)
                .bmr(1700.0)
                .tdee(2300.0)
                .dailyCalorieGoal(2300)
                .build();

        when(userProfileService.getMyHealthMetrics()).thenReturn(response);

        mockMvc.perform(get("/api/profiles/me/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bmi").value(22.8))
                .andExpect(jsonPath("$.data.dailyCalorieGoal").value(2300));
    }
}
