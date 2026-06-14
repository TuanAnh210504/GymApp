package com.aigym.service.impl;

import com.aigym.domain.entity.User;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.domain.enums.GoalType;
import com.aigym.dto.WeeklySchedule.ScheduleDayResponse;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.security.CurrentUserService;
import com.aigym.service.UserProfileService;
import com.aigym.service.WeeklyScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextGathererServiceImplTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private WeeklyScheduleService weeklyScheduleService;

    @InjectMocks
    private ContextGathererServiceImpl contextGathererService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void gatherUserContext_Success() {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setGoalType(GoalType.LOSE_WEIGHT);

        WeeklyScheduleResponse schedule = new WeeklyScheduleResponse();
        ScheduleDayResponse day = new ScheduleDayResponse();
        day.setRestDay(false);
        day.setScheduledExercises(Collections.emptyList());
        schedule.setScheduleDays(List.of(day));

        when(userProfileService.getMyProfile()).thenReturn(profile);
        when(weeklyScheduleService.getMyActiveSchedule()).thenReturn(schedule);

        String context = contextGathererService.gatherUserContext();

        assertTrue(context.contains("AIGym Personal Trainer"));
        verify(userProfileService, times(1)).getMyProfile();
        verify(weeklyScheduleService, times(1)).getMyActiveSchedule();
    }

    @Test
    void gatherUserContext_Fail_NoProfileAndNoSchedule() {
        when(userProfileService.getMyProfile()).thenThrow(new RuntimeException("Profile not found"));
        when(weeklyScheduleService.getMyActiveSchedule()).thenThrow(new RuntimeException("Schedule not found"));

        String context = assertDoesNotThrow(() -> contextGathererService.gatherUserContext());

        assertTrue(context.contains("Bạn là AIGym Personal Trainer"));
        assertTrue(context.contains("Không thể lấy lịch tập hiện tại."));
    }
}
