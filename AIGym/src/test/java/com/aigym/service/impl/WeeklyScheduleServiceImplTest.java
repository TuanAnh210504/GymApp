package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.*;
import com.aigym.domain.enums.Role;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleRequest;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ExerciseRepository;
import com.aigym.repository.ScheduleDayRepository;
import com.aigym.repository.WeeklyScheduleRepository;
import com.aigym.repository.WorkoutPlanRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyScheduleServiceImplTest {

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private ScheduleDayRepository scheduleDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private WeeklyScheduleServiceImpl weeklyScheduleService;

    private User currentUser;
    private WeeklySchedule schedule;
    private WeeklyScheduleRequest request;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").role(Role.USER).build();
        currentUser.setId(1L);

        schedule = new WeeklySchedule();
        schedule.setId(1L);
        schedule.setName("My Schedule");
        schedule.setUser(currentUser);
        schedule.setActive(true);

        request = new WeeklyScheduleRequest();
        request.setName("My Schedule");
        request.setActive(true);
    }

    @Test
    void createWeeklySchedule_Success_FromScratch() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        // Deactivate mock
        when(weeklyScheduleRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.of(new WeeklySchedule()));
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(schedule);
        // toResponse uses genericMapper for User
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyScheduleResponse res = weeklyScheduleService.createWeeklySchedule(request);

        assertNotNull(res);
        assertEquals("My Schedule", res.getName());
        verify(weeklyScheduleRepository, times(2)).save(any(WeeklySchedule.class)); // 1 for deactivate, 1 for new schedule
    }

    @Test
    void createWeeklySchedule_Success_FromTemplate() {
        request.setWorkoutPlanId(1L);
        WorkoutPlan plan = new WorkoutPlan();
        
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyScheduleRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(schedule);
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyScheduleResponse res = weeklyScheduleService.createWeeklySchedule(request);

        assertNotNull(res);
        verify(workoutPlanRepository, times(1)).findById(1L);
    }

    @Test
    void getWeeklyScheduleById_Success() {
        when(weeklyScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyScheduleResponse res = weeklyScheduleService.getWeeklyScheduleById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void getWeeklyScheduleById_Fail_NotOwner() {
        User otherUser = User.builder().build();
        otherUser.setId(2L);
        when(weeklyScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(BadRequestException.class, () -> weeklyScheduleService.getWeeklyScheduleById(1L));
    }

    @Test
    void getMyWeeklySchedules_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyScheduleRepository.findByUserId(1L)).thenReturn(List.of(schedule));

        List<WeeklyScheduleResponse> res = weeklyScheduleService.getMyWeeklySchedules();

        assertFalse(res.isEmpty());
        assertEquals(1, res.size());
    }

    @Test
    void getMyActiveSchedule_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyScheduleRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.of(schedule));
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyScheduleResponse res = weeklyScheduleService.getMyActiveSchedule();

        assertNotNull(res);
        assertTrue(res.isActive());
    }

    @Test
    void updateWeeklySchedule_Success() {
        request.setName("Updated Schedule");
        when(weeklyScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(schedule);
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyScheduleResponse res = weeklyScheduleService.updateWeeklySchedule(1L, request);

        assertNotNull(res);
        verify(scheduleDayRepository, times(1)).hardDeleteExercisesByScheduleId(1L);
        verify(scheduleDayRepository, times(1)).hardDeleteDaysByScheduleId(1L);
    }

    @Test
    void setActiveSchedule_Success() {
        schedule.setActive(false);
        when(weeklyScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyScheduleRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(schedule);

        WeeklyScheduleResponse res = weeklyScheduleService.setActiveSchedule(1L);

        assertNotNull(res);
        verify(weeklyScheduleRepository, times(1)).save(schedule);
    }

    @Test
    void deleteWeeklySchedule_Success() {
        when(weeklyScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertDoesNotThrow(() -> weeklyScheduleService.deleteWeeklySchedule(1L));
        verify(weeklyScheduleRepository, times(1)).delete(schedule);
    }
}
