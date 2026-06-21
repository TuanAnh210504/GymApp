package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.MealPlan.WeeklyMealPlanRequest;
import com.aigym.dto.MealPlan.WeeklyMealPlanResponse;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.service.WeeklyMealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
public class WeeklyMealPlanController {

    private final WeeklyMealPlanService weeklyMealPlanService;

    @PostMapping
    public ResponseEntity<ApiResponse<WeeklyMealPlanResponse>> createWeeklyMealPlan(
            @Valid @RequestBody WeeklyMealPlanRequest request) {
        WeeklyMealPlanResponse response = weeklyMealPlanService.createWeeklyMealPlan(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thực đơn thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklyMealPlanResponse>> getWeeklyMealPlanById(@PathVariable Long id) {
        WeeklyMealPlanResponse response = weeklyMealPlanService.getWeeklyMealPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WeeklyMealPlanResponse>>> getMyWeeklyMealPlans() {
        List<WeeklyMealPlanResponse> responses = weeklyMealPlanService.getMyWeeklyMealPlans();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/me/active")
    public ResponseEntity<ApiResponse<WeeklyMealPlanResponse>> getMyActiveMealPlan() {
        WeeklyMealPlanResponse response = weeklyMealPlanService.getMyActiveMealPlan();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklyMealPlanResponse>> updateWeeklyMealPlan(
            @PathVariable Long id,
            @Valid @RequestBody WeeklyMealPlanRequest request) {
        WeeklyMealPlanResponse response = weeklyMealPlanService.updateWeeklyMealPlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thực đơn thành công", response));
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<ApiResponse<WeeklyMealPlanResponse>> setActiveMealPlan(@PathVariable Long id) {
        WeeklyMealPlanResponse response = weeklyMealPlanService.setActiveMealPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt thực đơn thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWeeklyMealPlan(@PathVariable Long id) {
        weeklyMealPlanService.deleteWeeklyMealPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá thực đơn thành công", null));
    }

    @PutMapping("/meals/{mealId}/eaten")
    public ResponseEntity<ApiResponse<NutritionLogResponse>> markMealAsEaten(
            @PathVariable Long mealId,
            @RequestBody Map<String, Double> payload) {
        Double actualAmount = payload.get("actualAmount");
        NutritionLogResponse response = weeklyMealPlanService.markMealAsEaten(mealId, actualAmount);
        return ResponseEntity.ok(ApiResponse.success("Đã ghi lại vào nhật ký thành công", response));
    }

    @PutMapping("/meals/{mealId}/amount")
    public ResponseEntity<ApiResponse<com.aigym.dto.MealPlan.PlannedMealResponse>> updateMealAmount(
            @PathVariable Long mealId,
            @RequestBody Map<String, Double> payload) {
        Double newAmount = payload.get("amount");
        if (newAmount == null || newAmount <= 0) {
            throw new com.aigym.common.exception.BadRequestException("Khối lượng không hợp lệ");
        }
        com.aigym.dto.MealPlan.PlannedMealResponse response = weeklyMealPlanService.updateMealAmount(mealId, newAmount);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khối lượng thành công", response));
    }
}
