package com.aigym.controller;

import com.aigym.dto.ProgressPhoto.ProgressPhotoRequest;
import com.aigym.dto.ProgressPhoto.ProgressPhotoResponse;
import com.aigym.service.ProgressPhotoService;
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

@WebMvcTest(ProgressPhotoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProgressPhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressPhotoService progressPhotoService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @Test
    @WithMockUser
    void createProgressPhoto_Success() throws Exception {
        String jsonPayload = """
            {
              "imageUrl": "http://image.com/1.jpg",
              "weightAtTime": 70.0,
              "capturedAt": "2023-10-01"
            }
        """;

        ProgressPhotoResponse response = new ProgressPhotoResponse();
        response.setId(1L);
        response.setImageUrl("http://image.com/1.jpg");

        when(progressPhotoService.createProgressPhoto(any(ProgressPhotoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/progress-photos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Thêm ảnh tiến độ thành công"))
                .andExpect(jsonPath("$.data.imageUrl").value("http://image.com/1.jpg"));
    }

    @Test
    @WithMockUser
    void getProgressPhotoById_Success() throws Exception {
        ProgressPhotoResponse response = new ProgressPhotoResponse();
        response.setId(1L);
        response.setImageUrl("http://image.com/1.jpg");

        when(progressPhotoService.getProgressPhotoById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/progress-photos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value("http://image.com/1.jpg"));
    }

    @Test
    @WithMockUser
    void getMyProgressPhotos_Success() throws Exception {
        ProgressPhotoResponse response = new ProgressPhotoResponse();
        response.setId(1L);
        response.setImageUrl("http://image.com/1.jpg");

        when(progressPhotoService.getMyProgressPhotos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/progress-photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].imageUrl").value("http://image.com/1.jpg"));
    }

    @Test
    @WithMockUser
    void updateProgressPhoto_Success() throws Exception {
        String jsonPayload = """
            {
              "imageUrl": "http://image.com/2.jpg",
              "weightAtTime": 75.0,
              "capturedAt": "2023-10-01"
            }
        """;

        ProgressPhotoResponse response = new ProgressPhotoResponse();
        response.setId(1L);
        response.setImageUrl("http://image.com/2.jpg");

        when(progressPhotoService.updateProgressPhoto(eq(1L), any(ProgressPhotoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/progress-photos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật ảnh tiến độ thành công"))
                .andExpect(jsonPath("$.data.imageUrl").value("http://image.com/2.jpg"));
    }

    @Test
    @WithMockUser
    void deleteProgressPhoto_Success() throws Exception {
        doNothing().when(progressPhotoService).deleteProgressPhoto(1L);

        mockMvc.perform(delete("/api/progress-photos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá ảnh tiến độ thành công"));
    }
}
