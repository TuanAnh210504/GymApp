package com.aigym.service.impl;

import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.User;
import com.aigym.domain.entity.UserProfile;
import com.aigym.dto.UserProfile.UserProfileRequest;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.UserProfileRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.HealthCalculatorService;
import com.aigym.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;
    private final HealthCalculatorService healthCalculatorService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User currentUser = currentUserService.getCurrentUser();
        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Hồ sơ chưa được tạo. Vui lòng cập nhật thông tin."));
        return toResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse createOrUpdateMyProfile(UserProfileRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElse(new UserProfile()); // Nếu chưa có thì tạo mới

        profile.setUser(currentUser);
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setTargetWeight(request.getTargetWeight());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setActivityLevel(request.getActivityLevel());
        profile.setGoalType(request.getGoalType());

        // Auto-calculate Daily Calorie Goal (Overrides frontend value)
        Integer calculatedCalorieGoal = healthCalculatorService.calculateDailyCalorieGoal(profile);
        profile.setDailyCalorieGoal(calculatedCalorieGoal);

        // Auto-calculate Macros
        profile.setDailyProteinGoal(healthCalculatorService.calculateProteinGoal(calculatedCalorieGoal));
        profile.setDailyCarbsGoal(healthCalculatorService.calculateCarbsGoal(calculatedCalorieGoal));
        profile.setDailyFatsGoal(healthCalculatorService.calculateFatsGoal(calculatedCalorieGoal));
        profile.setDailyFiberGoal(healthCalculatorService.calculateFiberGoal(calculatedCalorieGoal));

        UserProfile saved = userProfileRepository.save(profile);
        return toResponse(saved);
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        UserProfileResponse response = genericMapper.mapToDto(profile, UserProfileResponse.class);
        if (profile.getUser() != null) {
            response.setUser(genericMapper.mapToDto(profile.getUser(), UserResponse.class));
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public com.aigym.dto.UserProfile.HealthMetricsResponse getMyHealthMetrics() {
        User currentUser = currentUserService.getCurrentUser();
        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Hồ sơ chưa được tạo. Vui lòng cập nhật thông tin."));

        int age = healthCalculatorService.calculateAge(profile.getDateOfBirth(), java.time.LocalDate.now());
        double bmi = healthCalculatorService.calculateBMI(profile.getWeight(), profile.getHeight());
        double bmr = healthCalculatorService.calculateBMR(profile.getWeight(), profile.getHeight(), age,
                profile.getGender());
        double tdee = healthCalculatorService.calculateTDEE(bmr, profile.getActivityLevel());
        int dailyCalorieGoal = healthCalculatorService.calculateDailyCalorieGoal(tdee, profile.getGoalType(),
                profile.getGender());

        return com.aigym.dto.UserProfile.HealthMetricsResponse.builder()
                .bmi(bmi)
                .bmr(bmr)
                .tdee(tdee)
                .dailyCalorieGoal(dailyCalorieGoal)
                .dailyProteinGoal(healthCalculatorService.calculateProteinGoal(dailyCalorieGoal))
                .dailyCarbsGoal(healthCalculatorService.calculateCarbsGoal(dailyCalorieGoal))
                .dailyFatsGoal(healthCalculatorService.calculateFatsGoal(dailyCalorieGoal))
                .dailyFiberGoal(healthCalculatorService.calculateFiberGoal(dailyCalorieGoal))
                .build();
    }
}
