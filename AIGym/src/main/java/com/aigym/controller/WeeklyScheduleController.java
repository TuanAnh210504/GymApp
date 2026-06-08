package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleRequest;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.service.WeeklyScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class WeeklyScheduleController {

    private final WeeklyScheduleService weeklyScheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<WeeklyScheduleResponse>> createWeeklySchedule(
            @Valid @RequestBody WeeklyScheduleRequest request) {
        WeeklyScheduleResponse response = weeklyScheduleService.createWeeklySchedule(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo lịch tập thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklyScheduleResponse>> getWeeklyScheduleById(@PathVariable Long id) {
        WeeklyScheduleResponse response = weeklyScheduleService.getWeeklyScheduleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WeeklyScheduleResponse>>> getMyWeeklySchedules() {
        List<WeeklyScheduleResponse> responses = weeklyScheduleService.getMyWeeklySchedules();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/me/active")
    public ResponseEntity<ApiResponse<WeeklyScheduleResponse>> getMyActiveSchedule() {
        WeeklyScheduleResponse response = weeklyScheduleService.getMyActiveSchedule();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklyScheduleResponse>> updateWeeklySchedule(
            @PathVariable Long id,
            @Valid @RequestBody WeeklyScheduleRequest request) {
        WeeklyScheduleResponse response = weeklyScheduleService.updateWeeklySchedule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch tập thành công", response));
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<ApiResponse<WeeklyScheduleResponse>> setActiveSchedule(@PathVariable Long id) {
        WeeklyScheduleResponse response = weeklyScheduleService.setActiveSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt lịch tập thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWeeklySchedule(@PathVariable Long id) {
        weeklyScheduleService.deleteWeeklySchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá lịch tập thành công", null));
    }
}
