package com.aigym.controller;

import com.aigym.dto.WorkoutSession.WorkoutSessionRequest;
import com.aigym.dto.WorkoutSession.WorkoutSessionResponse;
import com.aigym.service.WorkoutSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutSessionService workoutSessionService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    @WithMockUser
    void createWorkoutSession_Success() throws Exception {
        String jsonPayload = """
            {
              "startTime": "2023-10-01T10:00:00",
              "endTime": "2023-10-01T11:00:00",
              "totalCaloriesBurned": 300,
              "notes": "Good workout"
            }
        """;

        WorkoutSessionResponse response = new WorkoutSessionResponse();
        response.setId(1L);
        response.setTotalCaloriesBurned(300);

        when(workoutSessionService.createWorkoutSession(any(WorkoutSessionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Ghi nhận buổi tập thành công"))
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(300));
    }

    @Test
    @WithMockUser
    void getWorkoutSessionById_Success() throws Exception {
        WorkoutSessionResponse response = new WorkoutSessionResponse();
        response.setId(1L);
        response.setTotalCaloriesBurned(300);

        when(workoutSessionService.getWorkoutSessionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(300));
    }

    @Test
    @WithMockUser
    void getMySessionHistory_Success() throws Exception {
        WorkoutSessionResponse response = new WorkoutSessionResponse();
        response.setId(1L);
        response.setTotalCaloriesBurned(300);

        when(workoutSessionService.getMySessionHistory()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/sessions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].totalCaloriesBurned").value(300));
    }

    @Test
    @WithMockUser
    void updateWorkoutSession_Success() throws Exception {
        String jsonPayload = """
            {
              "startTime": "2023-10-01T10:00:00",
              "endTime": "2023-10-01T11:00:00",
              "totalCaloriesBurned": 400,
              "notes": "Good workout"
            }
        """;

        WorkoutSessionResponse response = new WorkoutSessionResponse();
        response.setId(1L);
        response.setTotalCaloriesBurned(400);

        when(workoutSessionService.updateWorkoutSession(eq(1L), any(WorkoutSessionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/sessions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật buổi tập thành công"))
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(400));
    }

    @Test
    @WithMockUser
    void deleteWorkoutSession_Success() throws Exception {
        doNothing().when(workoutSessionService).deleteWorkoutSession(1L);

        mockMvc.perform(delete("/api/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá buổi tập thành công"));
    }
}
