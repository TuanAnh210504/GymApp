package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.NutritionLog.NutritionLogRequest;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.service.NutritionLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition-logs")
@RequiredArgsConstructor
public class NutritionLogController {

    private final NutritionLogService nutritionLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<NutritionLogResponse>> createNutritionLog(
            @Valid @RequestBody NutritionLogRequest request) {
        NutritionLogResponse response = nutritionLogService.createNutritionLog(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm nhật ký dinh dưỡng thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionLogResponse>> getNutritionLogById(@PathVariable Long id) {
        NutritionLogResponse response = nutritionLogService.getNutritionLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NutritionLogResponse>>> getMyLogsByDate(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<NutritionLogResponse> responses = nutritionLogService.getMyLogsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionLogResponse>> updateNutritionLog(
            @PathVariable Long id,
            @Valid @RequestBody NutritionLogRequest request) {
        NutritionLogResponse response = nutritionLogService.updateNutritionLog(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhật ký dinh dưỡng thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNutritionLog(@PathVariable Long id) {
        nutritionLogService.deleteNutritionLog(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá nhật ký dinh dưỡng thành công", null));
    }
}
