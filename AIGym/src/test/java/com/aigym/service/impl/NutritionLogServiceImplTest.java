package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.FoodItem;
import com.aigym.domain.entity.NutritionLog;
import com.aigym.domain.entity.User;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.dto.NutritionLog.NutritionLogRequest;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.FoodItemRepository;
import com.aigym.repository.NutritionLogRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionLogServiceImplTest {

    @Mock
    private NutritionLogRepository nutritionLogRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private NutritionLogServiceImpl nutritionLogService;

    private User currentUser;
    private FoodItem foodItem;
    private NutritionLog nutritionLog;
    private NutritionLogRequest request;
    private NutritionLogResponse response;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").build();
        currentUser.setId(1L);

        foodItem = new FoodItem();
        foodItem.setId(10L);
        foodItem.setName("Apple");

        nutritionLog = new NutritionLog();
        nutritionLog.setId(1L);
        nutritionLog.setUser(currentUser);
        nutritionLog.setFoodItem(foodItem);
        nutritionLog.setLoggedAt(LocalDate.now());

        request = new NutritionLogRequest();
        request.setFoodItemId(10L);
        request.setLoggedAt(LocalDate.now());
        request.setAmount(100.0);

        response = new NutritionLogResponse();
        response.setId(1L);
        FoodItemResponse foodResp = new FoodItemResponse();
        foodResp.setName("Apple");
        response.setFoodItem(foodResp);
    }

    @Test
    void createNutritionLog_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));
        when(genericMapper.mapToEntity(request, NutritionLog.class)).thenReturn(nutritionLog);
        when(nutritionLogRepository.save(any(NutritionLog.class))).thenReturn(nutritionLog);
        when(genericMapper.mapToDto(nutritionLog, NutritionLogResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(response.getFoodItem());

        NutritionLogResponse res = nutritionLogService.createNutritionLog(request);

        assertNotNull(res);
        assertEquals("Apple", res.getFoodItem().getName());
        verify(nutritionLogRepository, times(1)).save(nutritionLog);
    }

    @Test
    void createNutritionLog_Fail_FoodItemNotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(foodItemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> nutritionLogService.createNutritionLog(request));
    }

    @Test
    void getNutritionLogById_Success() {
        when(nutritionLogRepository.findById(1L)).thenReturn(Optional.of(nutritionLog));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToDto(nutritionLog, NutritionLogResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(response.getFoodItem());

        NutritionLogResponse res = nutritionLogService.getNutritionLogById(1L);

        assertNotNull(res);
    }

    @Test
    void getNutritionLogById_Fail_NotOwner() {
        User otherUser = User.builder().build();
        otherUser.setId(2L);
        when(nutritionLogRepository.findById(1L)).thenReturn(Optional.of(nutritionLog));
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(BadRequestException.class, () -> nutritionLogService.getNutritionLogById(1L));
    }

    @Test
    void getMyLogsByDate_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(nutritionLogRepository.findByUserIdAndLoggedAtOrderByMealTypeAsc(1L, LocalDate.now())).thenReturn(List.of(nutritionLog));
        when(genericMapper.mapToDto(nutritionLog, NutritionLogResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(response.getFoodItem());

        List<NutritionLogResponse> res = nutritionLogService.getMyLogsByDate(LocalDate.now());

        assertFalse(res.isEmpty());
        assertEquals(1, res.size());
    }

    @Test
    void updateNutritionLog_Success() {
        when(nutritionLogRepository.findById(1L)).thenReturn(Optional.of(nutritionLog));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));
        when(nutritionLogRepository.save(any(NutritionLog.class))).thenReturn(nutritionLog);
        when(genericMapper.mapToDto(nutritionLog, NutritionLogResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(response.getFoodItem());

        NutritionLogResponse res = nutritionLogService.updateNutritionLog(1L, request);

        assertNotNull(res);
        verify(nutritionLogRepository, times(1)).save(nutritionLog);
    }

    @Test
    void deleteNutritionLog_Success() {
        when(nutritionLogRepository.findById(1L)).thenReturn(Optional.of(nutritionLog));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertDoesNotThrow(() -> nutritionLogService.deleteNutritionLog(1L));
        verify(nutritionLogRepository, times(1)).delete(nutritionLog);
    }
}
