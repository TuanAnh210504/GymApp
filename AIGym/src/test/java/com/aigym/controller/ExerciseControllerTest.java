package com.aigym.controller;

import com.aigym.domain.enums.Category;
import com.aigym.dto.Exercise.ExerciseRequest;
import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(ExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createExercise_Success() throws Exception {
        String jsonPayload = """
            {
              "name": "Push Up",
              "description": "A classic chest exercise",
              "primaryCategory": "CHEST_MIDDLE",
              "difficulty": "NORMAL",
              "isPublic": true
            }
        """;

        ExerciseResponse response = new ExerciseResponse();
        response.setId(1L);
        response.setName("Push Up");

        when(exerciseService.createExercise(any(ExerciseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo bài tập thành công"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Push Up"));
    }

    @Test
    void getExerciseById_Success() throws Exception {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(1L);
        response.setName("Push Up");

        when(exerciseService.getExerciseById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Push Up"));
    }

    @Test
    void getAllExercises_Success() throws Exception {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(1L);
        response.setName("Push Up");

        when(exerciseService.getAllExercises()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Push Up"));
    }

    @Test
    void getByPrimaryCategory_Success() throws Exception {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(1L);
        response.setName("Push Up");

        when(exerciseService.getExercisesByPrimaryCategory(Category.CHEST_MIDDLE)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/exercises/category/CHEST_MIDDLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Push Up"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateExercise_Success() throws Exception {
        String jsonPayload = """
            {
              "name": "Pull Up",
              "description": "A classic back exercise",
              "primaryCategory": "LATS",
              "difficulty": "NORMAL",
              "isPublic": true
            }
        """;

        ExerciseResponse response = new ExerciseResponse();
        response.setId(1L);
        response.setName("Pull Up");

        when(exerciseService.updateExercise(eq(1L), any(ExerciseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật bài tập thành công"))
                .andExpect(jsonPath("$.data.name").value("Pull Up"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteExercise_Success() throws Exception {
        doNothing().when(exerciseService).deleteExercise(1L);

        mockMvc.perform(delete("/api/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá bài tập thành công"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void bulkDeleteExercises_Success() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        doNothing().when(exerciseService).bulkDeleteExercises(ids);

        mockMvc.perform(delete("/api/exercises/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá 2 bài tập thành công"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeletedExercises_Success() throws Exception {
        ExerciseResponse response = new ExerciseResponse();
        response.setId(1L);
        response.setName("Push Up (Deleted)");

        when(exerciseService.getDeletedExercises()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/exercises/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Push Up (Deleted)"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void restoreExercise_Success() throws Exception {
        doNothing().when(exerciseService).restoreExercise(1L);

        mockMvc.perform(put("/api/exercises/1/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Khôi phục bài tập thành công"));
    }
}
