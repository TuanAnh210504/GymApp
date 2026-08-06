package com.aigym.controller;

import com.aigym.dto.FoodItem.FoodItemRequest;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.service.FoodItemService;
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

@WebMvcTest(FoodItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class FoodItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoodItemService foodItemService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFoodItem_Success() throws Exception {
        String jsonPayload = """
            {
              "name": "Apple",
              "brand": "Fresh",
              "caloriesPer100g": 52,
              "protein": 0.3,
              "carbs": 14.0,
              "fat": 0.2,
              "fiber": 2.4
            }
        """;

        FoodItemResponse response = new FoodItemResponse();
        response.setId(1L);
        response.setName("Apple");

        when(foodItemService.createFoodItem(any(FoodItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/food-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Thêm thực phẩm thành công"))
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }

    @Test
    @WithMockUser
    void getFoodItemById_Success() throws Exception {
        FoodItemResponse response = new FoodItemResponse();
        response.setId(1L);
        response.setName("Apple");

        when(foodItemService.getFoodItemById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/food-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }

    @Test
    @WithMockUser
    void getAllFoodItems_Success() throws Exception {
        FoodItemResponse response = new FoodItemResponse();
        response.setId(1L);
        response.setName("Apple");

        when(foodItemService.getAllFoodItems()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/food-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Apple"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFoodItem_Success() throws Exception {
        String jsonPayload = """
            {
              "name": "Banana",
              "caloriesPer100g": 89,
              "protein": 1.1,
              "carbs": 22.8,
              "fat": 0.3,
              "fiber": 2.6
            }
        """;

        FoodItemResponse response = new FoodItemResponse();
        response.setId(1L);
        response.setName("Banana");

        when(foodItemService.updateFoodItem(eq(1L), any(FoodItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/food-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật thực phẩm thành công"))
                .andExpect(jsonPath("$.data.name").value("Banana"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFoodItem_Success() throws Exception {
        doNothing().when(foodItemService).deleteFoodItem(1L);

        mockMvc.perform(delete("/api/food-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xoá thực phẩm thành công"));
    }
}
