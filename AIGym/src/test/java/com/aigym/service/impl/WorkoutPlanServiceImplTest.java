package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.Exercise;
import com.aigym.domain.entity.User;
import com.aigym.domain.entity.WorkoutPlan;
import com.aigym.domain.enums.Difficulty;
import com.aigym.domain.enums.Role;
import com.aigym.dto.WorkoutPlan.WorkoutPlanRequest;
import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;
import com.aigym.dto.WorkoutPlan.WorkoutPlanDayRequest;
import com.aigym.dto.WorkoutPlan.WorkoutPlanExerciseRequest;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ExerciseRepository;
import com.aigym.repository.WorkoutPlanDayRepository;
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
class WorkoutPlanServiceImplTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private WorkoutPlanDayRepository workoutPlanDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private WorkoutPlanServiceImpl workoutPlanService;

    private User currentUser;
    private WorkoutPlan plan;
    private WorkoutPlanRequest request;
    private WorkoutPlanResponse response;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").role(Role.USER).build();
        currentUser.setId(1L);

        plan = new WorkoutPlan();
        plan.setId(1L);
        plan.setTitle("Plan 1");
        plan.setCreator(currentUser);

        request = new WorkoutPlanRequest();
        request.setTitle("Plan 1");
        request.setDifficulty(Difficulty.NORMAL);

        response = new WorkoutPlanResponse();
        response.setId(1L);
        response.setTitle("Plan 1");

        exercise = new Exercise();
        exercise.setId(1L);
    }

    @Test
    void createWorkoutPlan_Success() {
        when(workoutPlanRepository.existsByTitle("Plan 1")).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToEntity(request, WorkoutPlan.class)).thenReturn(plan);
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(plan);
        when(genericMapper.mapToDto(plan, WorkoutPlanResponse.class)).thenReturn(response);

        WorkoutPlanResponse res = workoutPlanService.createWorkoutPlan(request);

        assertNotNull(res);
        assertEquals("Plan 1", res.getTitle());
        verify(workoutPlanRepository, times(1)).save(any(WorkoutPlan.class));
    }

    @Test
    void createWorkoutPlan_Fail_TitleExists() {
        when(workoutPlanRepository.existsByTitle("Plan 1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> workoutPlanService.createWorkoutPlan(request));
    }

    @Test
    void getWorkoutPlanById_Success() {
        when(workoutPlanRepository.findWithDetailsById(1L)).thenReturn(Optional.of(plan));
        when(genericMapper.mapToDto(plan, WorkoutPlanResponse.class)).thenReturn(response);

        WorkoutPlanResponse res = workoutPlanService.getWorkoutPlanById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void updateWorkoutPlan_Success() {
        request.setTitle("Plan 2");
        when(workoutPlanRepository.findWithDetailsById(1L)).thenReturn(Optional.of(plan));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workoutPlanRepository.existsByTitle("Plan 2")).thenReturn(false);
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(plan);
        
        WorkoutPlanResponse updatedResp = new WorkoutPlanResponse();
        updatedResp.setTitle("Plan 2");
        when(genericMapper.mapToDto(plan, WorkoutPlanResponse.class)).thenReturn(updatedResp);

        WorkoutPlanResponse res = workoutPlanService.updateWorkoutPlan(1L, request);

        assertNotNull(res);
        assertEquals("Plan 2", res.getTitle());
        verify(workoutPlanDayRepository, times(1)).deleteExercisesByPlanIdNative(1L);
        verify(workoutPlanDayRepository, times(1)).deleteDaysByPlanIdNative(1L);
    }

    @Test
    void updateWorkoutPlan_Fail_NotOwner() {
        User otherUser = User.builder().role(Role.USER).build();
        otherUser.setId(2L);
        when(workoutPlanRepository.findWithDetailsById(1L)).thenReturn(Optional.of(plan));
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(BadRequestException.class, () -> workoutPlanService.updateWorkoutPlan(1L, request));
    }

    @Test
    void deleteWorkoutPlan_Success() {
        when(workoutPlanRepository.findWithDetailsById(1L)).thenReturn(Optional.of(plan));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertDoesNotThrow(() -> workoutPlanService.deleteWorkoutPlan(1L));
        verify(workoutPlanRepository, times(1)).delete(plan);
    }

    @Test
    void bulkDeleteWorkoutPlans_Success() {
        List<Long> ids = List.of(1L, 2L);
        when(workoutPlanRepository.findAllById(ids)).thenReturn(List.of(plan, new WorkoutPlan()));

        assertDoesNotThrow(() -> workoutPlanService.bulkDeleteWorkoutPlans(ids));
        verify(workoutPlanRepository, times(1)).deleteAll(any());
    }

    @Test
    void restoreWorkoutPlan_Success() {
        when(workoutPlanRepository.restoreNative(1L)).thenReturn(1);

        assertDoesNotThrow(() -> workoutPlanService.restoreWorkoutPlan(1L));
        verify(workoutPlanRepository, times(1)).restoreNative(1L);
    }
}
