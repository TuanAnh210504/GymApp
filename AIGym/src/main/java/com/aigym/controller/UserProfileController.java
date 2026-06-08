package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.UserProfile.UserProfileRequest;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userProfileService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createOrUpdateMyProfile(
            @Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse response = userProfileService.createOrUpdateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Lưu hồ sơ thành công", response));
    }

    @GetMapping("/me/metrics")
    public ResponseEntity<ApiResponse<com.aigym.dto.UserProfile.HealthMetricsResponse>> getMyHealthMetrics() {
        com.aigym.dto.UserProfile.HealthMetricsResponse response = userProfileService.getMyHealthMetrics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
