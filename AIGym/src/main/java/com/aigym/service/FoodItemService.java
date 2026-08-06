package com.aigym.service;

import com.aigym.dto.FoodItem.FoodItemRequest;
import com.aigym.dto.FoodItem.FoodItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FoodItemService {

    FoodItemResponse createFoodItem(FoodItemRequest request);

    FoodItemResponse getFoodItemById(Long id);

    /** Trả về tất cả thực phẩm với phân trang (khuyến nghị). */
    Page<FoodItemResponse> getAllFoodItems(Pageable pageable);

    /** Trả về toàn bộ list – chỉ dùng nội bộ. */
    List<FoodItemResponse> getAllFoodItems();

    FoodItemResponse updateFoodItem(Long id, FoodItemRequest request);

    void deleteFoodItem(Long id);
}
