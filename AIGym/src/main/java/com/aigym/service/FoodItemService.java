package com.aigym.service;

import com.aigym.dto.FoodItem.FoodItemRequest;
import com.aigym.dto.FoodItem.FoodItemResponse;

import java.util.List;

public interface FoodItemService {

    FoodItemResponse createFoodItem(FoodItemRequest request);

    FoodItemResponse getFoodItemById(Long id);

    List<FoodItemResponse> getAllFoodItems();

    FoodItemResponse updateFoodItem(Long id, FoodItemRequest request);

    void deleteFoodItem(Long id);
}
