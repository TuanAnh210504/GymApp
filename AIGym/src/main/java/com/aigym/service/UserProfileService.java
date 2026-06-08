package com.aigym.service;

import com.aigym.dto.UserProfile.UserProfileRequest;
import com.aigym.dto.UserProfile.UserProfileResponse;

import com.aigym.dto.UserProfile.HealthMetricsResponse;

public interface UserProfileService {
    UserProfileResponse getMyProfile();
    UserProfileResponse createOrUpdateMyProfile(UserProfileRequest request);
    HealthMetricsResponse getMyHealthMetrics();
}
