package com.aigym.controller;

import com.aigym.dto.NutritionLog.NutritionLogRequest;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.service.NutritionLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutritionLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class NutritionLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NutritionLogService nutritionLogService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void createNutritionLog_Success() throws Exception {
        String jsonPayload = """
            {
              "foodItemId": 1,
              "amount": 100.0,
              "mealType": "BREAKFAST",
              "loggedAt": "2023-10-01"
            }
        """;

        NutritionLogResponse response = new NutritionLogResponse();
        response.setId(1L);
        response.setAmount(100.0);

        when(nutritionLogService.createNutritionLog(any(NutritionLogRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/nutrition-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Thêm nhật ký dinh dưỡng thành công"))
                .andExpect(jsonPath("$.data.amount").value(100.0));
    }

    @Test
    @WithMockUser
    void getNutritionLogById_Success() throws Exception {
        NutritionLogResponse response = new NutritionLogResponse();
        response.setId(1L);
        response.setAmount(100.0);

        when(nutritionLogService.getNutritionLogById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/nutrition-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(100.0));
    }

    @Test
    @WithMockUser
    void getMyLogsByDate_Success() throws Exception {
        NutritionLogResponse response = new NutritionLogResponse();
        response.setId(1L);
        response.setAmount(100.0);

        when(nutritionLogService.getMyLogsByDate(any(LocalDate.class))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/nutrition-logs?date=2023-10-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].amount").value(100.0));
    }

    @Test
    @WithMockUser
    void updateNutritionLog_Success() throws Exception {
        String jsonPayload = """
            {
              "foodItemId": 1,
              "amount": 200.0,
              "mealType": "LUNCH",
              "loggedAt": "2023-10-01"
            }
        """;

        NutritionLogResponse response = new NutritionLogResponse();
        response.setId(1L);
        response.setAmount(200.0);

        when(nutritionLogService.updateNutritionLog(eq(1L), any(NutritionLogRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/nutrition-logs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật nhật ký dinh dưỡng thành công"))
                .andExpect(jsonPath("$.data.amount").value(200.0));
    }

    @Test
    @WithMockUser
    void deleteNutritionLog_Success() throws Exception {
        doNothing().when(nutritionLogService).deleteNutritionLog(1L);

        mockMvc.perform(delete("/api/nutrition-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá nhật ký dinh dưỡng thành công"));
    }
}
