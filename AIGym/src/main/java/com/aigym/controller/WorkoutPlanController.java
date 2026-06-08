package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.WorkoutPlan.WorkoutPlanRequest;
import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;
import com.aigym.service.WorkoutPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanRequest request) {
        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo giáo án thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> getWorkoutPlanById(@PathVariable Long id) {
        WorkoutPlanResponse response = workoutPlanService.getWorkoutPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> getPublicWorkoutPlans() {
        List<WorkoutPlanResponse> responses = workoutPlanService.getPublicWorkoutPlans();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> getMyWorkoutPlans() {
        List<WorkoutPlanResponse> responses = workoutPlanService.getMyWorkoutPlans();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> updateWorkoutPlan(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanRequest request) {
        WorkoutPlanResponse response = workoutPlanService.updateWorkoutPlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giáo án thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkoutPlan(@PathVariable Long id) {
        workoutPlanService.deleteWorkoutPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá giáo án thành công", null));
    }
}
