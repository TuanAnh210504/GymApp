package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.WorkoutSession.WorkoutSessionRequest;
import com.aigym.dto.WorkoutSession.WorkoutSessionResponse;
import com.aigym.service.WorkoutSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> createWorkoutSession(
            @Valid @RequestBody WorkoutSessionRequest request) {
        WorkoutSessionResponse response = workoutSessionService.createWorkoutSession(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ghi nhận buổi tập thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> getWorkoutSessionById(@PathVariable Long id) {
        WorkoutSessionResponse response = workoutSessionService.getWorkoutSessionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WorkoutSessionResponse>>> getMySessionHistory() {
        List<WorkoutSessionResponse> responses = workoutSessionService.getMySessionHistory();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> updateWorkoutSession(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutSessionRequest request) {
        WorkoutSessionResponse response = workoutSessionService.updateWorkoutSession(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật buổi tập thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkoutSession(@PathVariable Long id) {
        workoutSessionService.deleteWorkoutSession(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá buổi tập thành công", null));
    }
}
