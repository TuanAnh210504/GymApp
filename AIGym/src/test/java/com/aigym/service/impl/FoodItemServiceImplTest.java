package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.FoodItem;
import com.aigym.dto.FoodItem.FoodItemRequest;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodItemServiceImplTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private FoodItemServiceImpl foodItemService;

    private FoodItem foodItem;
    private FoodItemRequest request;
    private FoodItemResponse response;

    @BeforeEach
    void setUp() {
        foodItem = new FoodItem();
        foodItem.setId(1L);
        foodItem.setName("Apple");
        foodItem.setCaloriesPer100g(52);

        request = new FoodItemRequest();
        request.setName("Apple");
        request.setCaloriesPer100g(52);

        response = new FoodItemResponse();
        response.setId(1L);
        response.setName("Apple");
    }

    @Test
    void createFoodItem_Success() {
        when(foodItemRepository.existsByName("Apple")).thenReturn(false);
        when(genericMapper.mapToEntity(request, FoodItem.class)).thenReturn(foodItem);
        when(foodItemRepository.save(any(FoodItem.class))).thenReturn(foodItem);
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(response);

        FoodItemResponse res = foodItemService.createFoodItem(request);

        assertNotNull(res);
        assertEquals("Apple", res.getName());
        verify(foodItemRepository, times(1)).save(foodItem);
    }

    @Test
    void createFoodItem_Fail_Duplicate() {
        when(foodItemRepository.existsByName("Apple")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> foodItemService.createFoodItem(request));
    }

    @Test
    void getFoodItemById_Success() {
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(foodItem));
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(response);

        FoodItemResponse res = foodItemService.getFoodItemById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void getFoodItemById_Fail_NotFound() {
        when(foodItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> foodItemService.getFoodItemById(1L));
    }

    @Test
    void getAllFoodItems_Success() {
        when(foodItemRepository.findAll()).thenReturn(List.of(foodItem));
        when(genericMapper.mapListToDto(anyList(), eq(FoodItemResponse.class))).thenReturn(List.of(response));

        List<FoodItemResponse> res = foodItemService.getAllFoodItems();

        assertFalse(res.isEmpty());
        assertEquals(1, res.size());
    }

    @Test
    void updateFoodItem_Success() {
        request.setName("Banana");
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(foodItem));
        when(foodItemRepository.existsByName("Banana")).thenReturn(false);
        when(foodItemRepository.save(any(FoodItem.class))).thenReturn(foodItem);
        
        FoodItemResponse updatedResponse = new FoodItemResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("Banana");
        when(genericMapper.mapToDto(foodItem, FoodItemResponse.class)).thenReturn(updatedResponse);

        FoodItemResponse res = foodItemService.updateFoodItem(1L, request);

        assertNotNull(res);
        assertEquals("Banana", res.getName());
    }

    @Test
    void updateFoodItem_Fail_Duplicate() {
        request.setName("Banana");
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(foodItem));
        when(foodItemRepository.existsByName("Banana")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> foodItemService.updateFoodItem(1L, request));
    }

    @Test
    void deleteFoodItem_Success() {
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(foodItem));

        assertDoesNotThrow(() -> foodItemService.deleteFoodItem(1L));
        verify(foodItemRepository, times(1)).delete(foodItem);
    }
}
