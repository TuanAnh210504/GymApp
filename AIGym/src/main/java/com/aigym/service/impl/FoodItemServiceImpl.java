package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.FoodItem;
import com.aigym.dto.FoodItem.FoodItemRequest;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.FoodItemRepository;
import com.aigym.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public FoodItemResponse createFoodItem(FoodItemRequest request) {
        if (foodItemRepository.existsByName(request.getName())) {
            throw new BadRequestException("Thực phẩm với tên '" + request.getName() + "' đã tồn tại");
        }

        FoodItem foodItem = genericMapper.mapToEntity(request, FoodItem.class);
        FoodItem saved = foodItemRepository.save(foodItem);
        return genericMapper.mapToDto(saved, FoodItemResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemResponse getFoodItemById(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thực phẩm với ID: " + id));
        return genericMapper.mapToDto(foodItem, FoodItemResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getAllFoodItems() {
        List<FoodItem> foodItems = foodItemRepository.findAll();
        return genericMapper.mapListToDto(foodItems, FoodItemResponse.class);
    }

    @Override
    @Transactional
    public FoodItemResponse updateFoodItem(Long id, FoodItemRequest request) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thực phẩm với ID: " + id));

        if (!foodItem.getName().equals(request.getName()) && foodItemRepository.existsByName(request.getName())) {
            throw new BadRequestException("Thực phẩm với tên '" + request.getName() + "' đã tồn tại");
        }

        foodItem.setName(request.getName());
        foodItem.setBrand(request.getBrand());
        foodItem.setCaloriesPer100g(request.getCaloriesPer100g());
        foodItem.setProtein(request.getProtein());
        foodItem.setCarbs(request.getCarbs());
        foodItem.setFat(request.getFat());
        foodItem.setFiber(request.getFiber());

        FoodItem updated = foodItemRepository.save(foodItem);
        return genericMapper.mapToDto(updated, FoodItemResponse.class);
    }

    @Override
    @Transactional
    public void deleteFoodItem(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thực phẩm với ID: " + id));
        foodItemRepository.delete(foodItem);
    }
}
