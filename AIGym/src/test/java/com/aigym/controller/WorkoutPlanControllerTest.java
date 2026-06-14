package com.aigym.controller;

import com.aigym.domain.enums.Difficulty;
import com.aigym.dto.WorkoutPlan.WorkoutPlanRequest;
import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;
import com.aigym.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutPlanService workoutPlanService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    @WithMockUser
    void createWorkoutPlan_Success() throws Exception {
        String jsonPayload = """
            {
              "title": "Plan 1",
              "description": "Description",
              "difficulty": "NORMAL",
              "durationWeeks": 4,
              "isPublic": true
            }
        """;

        WorkoutPlanResponse response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 1");

        when(workoutPlanService.createWorkoutPlan(any(WorkoutPlanRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/workout-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo giáo án thành công"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Plan 1"));
    }

    @Test
    void getWorkoutPlanById_Success() throws Exception {
        WorkoutPlanResponse response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 1");

        when(workoutPlanService.getWorkoutPlanById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/workout-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Plan 1"));
    }

    @Test
    void getPublicWorkoutPlans_Success() throws Exception {
        WorkoutPlanResponse response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 1");

        when(workoutPlanService.getPublicWorkoutPlans()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/workout-plans/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Plan 1"));
    }

    @Test
    @WithMockUser
    void getMyWorkoutPlans_Success() throws Exception {
        WorkoutPlanResponse response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 1");

        when(workoutPlanService.getMyWorkoutPlans()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/workout-plans/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Plan 1"));
    }

    @Test
    @WithMockUser
    void updateWorkoutPlan_Success() throws Exception {
        String jsonPayload = """
            {
              "title": "Plan 2",
              "description": "Description",
              "difficulty": "NORMAL",
              "durationWeeks": 4,
              "isPublic": true
            }
        """;

        WorkoutPlanResponse response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 2");

        when(workoutPlanService.updateWorkoutPlan(eq(1L), any(WorkoutPlanRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/workout-plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật giáo án thành công"))
                .andExpect(jsonPath("$.data.title").value("Plan 2"));
    }

    @Test
    @WithMockUser
    void deleteWorkoutPlan_Success() throws Exception {
        doNothing().when(workoutPlanService).deleteWorkoutPlan(1L);

        mockMvc.perform(delete("/api/workout-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá giáo án thành công"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void bulkDeleteWorkoutPlans_Success() throws Exception {
        String jsonPayload = "[1, 2]";
        doNothing().when(workoutPlanService).bulkDeleteWorkoutPlans(List.of(1L, 2L));

        mockMvc.perform(delete("/api/workout-plans/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá 2 giáo án thành công"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeletedWorkoutPlans_Success() throws Exception {
        WorkoutPlanResponse response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 1");

        when(workoutPlanService.getDeletedWorkoutPlans()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/workout-plans/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Plan 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void restoreWorkoutPlan_Success() throws Exception {
        doNothing().when(workoutPlanService).restoreWorkoutPlan(1L);

        mockMvc.perform(put("/api/workout-plans/1/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Khôi phục giáo án thành công"));
    }
}
