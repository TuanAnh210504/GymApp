package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.FoodItem.FoodItemRequest;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.service.FoodItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food-items")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemResponse>> createFoodItem(
            @Valid @RequestBody FoodItemRequest request) {
        FoodItemResponse response = foodItemService.createFoodItem(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm thực phẩm thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodItemResponse>> getFoodItemById(@PathVariable Long id) {
        FoodItemResponse response = foodItemService.getFoodItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ── FIX: Thêm Pageable để tránh OOM khi db thực phẩm lên hàng vạn dòng.
     * Mặc định: page=0, size=20, sort=name,asc
     * Ví dụ: GET /api/food-items?page=0&size=20&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FoodItemResponse>>> getAllFoodItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortDir, sort));
        Page<FoodItemResponse> responses = foodItemService.getAllFoodItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodItemResponse>> updateFoodItem(
            @PathVariable Long id,
            @Valid @RequestBody FoodItemRequest request) {
        FoodItemResponse response = foodItemService.updateFoodItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thực phẩm thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFoodItem(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá thực phẩm thành công", null));
    }
}
