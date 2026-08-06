package com.aigym.controller;

import com.aigym.dto.WeeklySchedule.WeeklyScheduleRequest;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.service.WeeklyScheduleService;
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

@WebMvcTest(WeeklyScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeeklyScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeeklyScheduleService weeklyScheduleService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    @WithMockUser
    void createWeeklySchedule_Success() throws Exception {
        String jsonPayload = """
            {
              "name": "My Schedule",
              "description": "Desc",
              "active": true
            }
        """;

        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setId(1L);
        response.setName("My Schedule");

        when(weeklyScheduleService.createWeeklySchedule(any(WeeklyScheduleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo lịch tập thành công"))
                .andExpect(jsonPath("$.data.name").value("My Schedule"));
    }

    @Test
    @WithMockUser
    void getWeeklyScheduleById_Success() throws Exception {
        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setId(1L);
        response.setName("My Schedule");

        when(weeklyScheduleService.getWeeklyScheduleById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("My Schedule"));
    }

    @Test
    @WithMockUser
    void getMyWeeklySchedules_Success() throws Exception {
        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setId(1L);
        response.setName("My Schedule");

        when(weeklyScheduleService.getMyWeeklySchedules()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/schedules/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("My Schedule"));
    }

    @Test
    @WithMockUser
    void getMyActiveSchedule_Success() throws Exception {
        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setId(1L);
        response.setName("Active Schedule");

        when(weeklyScheduleService.getMyActiveSchedule()).thenReturn(response);

        mockMvc.perform(get("/api/schedules/me/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Active Schedule"));
    }

    @Test
    @WithMockUser
    void updateWeeklySchedule_Success() throws Exception {
        String jsonPayload = """
            {
              "name": "Updated Schedule",
              "description": "Desc",
              "active": true
            }
        """;

        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setId(1L);
        response.setName("Updated Schedule");

        when(weeklyScheduleService.updateWeeklySchedule(eq(1L), any(WeeklyScheduleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật lịch tập thành công"))
                .andExpect(jsonPath("$.data.name").value("Updated Schedule"));
    }

    @Test
    @WithMockUser
    void setActiveSchedule_Success() throws Exception {
        WeeklyScheduleResponse response = new WeeklyScheduleResponse();
        response.setId(1L);
        response.setName("My Schedule");
        response.setActive(true);

        when(weeklyScheduleService.setActiveSchedule(1L)).thenReturn(response);

        mockMvc.perform(put("/api/schedules/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Kích hoạt lịch tập thành công"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @WithMockUser
    void deleteWeeklySchedule_Success() throws Exception {
        doNothing().when(weeklyScheduleService).deleteWeeklySchedule(1L);

        mockMvc.perform(delete("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá lịch tập thành công"));
    }
}
