package com.aigym.service.impl;

import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.User;
import com.aigym.domain.entity.UserProfile;
import com.aigym.domain.enums.ActivityLevel;
import com.aigym.domain.enums.Gender;
import com.aigym.domain.enums.GoalType;
import com.aigym.dto.UserProfile.HealthMetricsResponse;
import com.aigym.dto.UserProfile.UserProfileRequest;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.UserProfileRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.HealthCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @Mock
    private HealthCalculatorService healthCalculatorService;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User currentUser;
    private UserProfile userProfile;
    private UserProfileRequest request;
    private UserProfileResponse response;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").build();
        currentUser.setId(1L);

        userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setUser(currentUser);
        userProfile.setWeight(70.0);
        userProfile.setHeight(175.0);
        userProfile.setDateOfBirth(LocalDate.of(1995, 1, 1));
        userProfile.setGender(Gender.MALE);
        userProfile.setActivityLevel(ActivityLevel.LIGHTLY_ACTIVE);
        userProfile.setGoalType(GoalType.MAINTAIN_WEIGHT);

        request = new UserProfileRequest();
        request.setWeight(75.0);
        request.setHeight(175.0);
        request.setDateOfBirth(LocalDate.of(1995, 1, 1));
        request.setGender(Gender.MALE);
        request.setActivityLevel(ActivityLevel.LIGHTLY_ACTIVE);
        request.setGoalType(GoalType.BUILD_MUSCLE);

        response = new UserProfileResponse();
        response.setId(1L);
        response.setWeight(75.0);
    }

    @Test
    void getMyProfile_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));
        when(genericMapper.mapToDto(userProfile, UserProfileResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(currentUser, UserResponse.class)).thenReturn(new UserResponse());

        UserProfileResponse res = userProfileService.getMyProfile();

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void getMyProfile_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userProfileService.getMyProfile());
    }

    @Test
    void createOrUpdateMyProfile_Create_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(healthCalculatorService.calculateDailyCalorieGoal(any(UserProfile.class))).thenReturn(2500);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(genericMapper.mapToDto(userProfile, UserProfileResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(currentUser, UserResponse.class)).thenReturn(new UserResponse());

        UserProfileResponse res = userProfileService.createOrUpdateMyProfile(request);

        assertNotNull(res);
        verify(healthCalculatorService, times(1)).calculateDailyCalorieGoal(any(UserProfile.class));
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void createOrUpdateMyProfile_Update_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));
        when(healthCalculatorService.calculateDailyCalorieGoal(any(UserProfile.class))).thenReturn(2600);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(genericMapper.mapToDto(userProfile, UserProfileResponse.class)).thenReturn(response);
        when(genericMapper.mapToDto(currentUser, UserResponse.class)).thenReturn(new UserResponse());

        UserProfileResponse res = userProfileService.createOrUpdateMyProfile(request);

        assertNotNull(res);
        verify(userProfileRepository, times(1)).save(userProfile);
    }

    @Test
    void getMyHealthMetrics_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));
        
        when(healthCalculatorService.calculateAge(any(), any())).thenReturn(30);
        when(healthCalculatorService.calculateBMI(70.0, 175.0)).thenReturn(22.8);
        when(healthCalculatorService.calculateBMR(70.0, 175.0, 30, Gender.MALE)).thenReturn(1700.0);
        when(healthCalculatorService.calculateTDEE(1700.0, ActivityLevel.LIGHTLY_ACTIVE)).thenReturn(2300.0);
        when(healthCalculatorService.calculateDailyCalorieGoal(2300.0, GoalType.MAINTAIN_WEIGHT, Gender.MALE)).thenReturn(2300);

        HealthMetricsResponse res = userProfileService.getMyHealthMetrics();

        assertNotNull(res);
        assertEquals(22.8, res.getBmi());
        assertEquals(2300, res.getDailyCalorieGoal());
    }

    @Test
    void getMyHealthMetrics_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userProfileService.getMyHealthMetrics());
    }
}
