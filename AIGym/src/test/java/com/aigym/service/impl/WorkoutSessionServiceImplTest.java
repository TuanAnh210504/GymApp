package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.Exercise;
import com.aigym.domain.entity.User;
import com.aigym.domain.entity.WorkoutPlan;
import com.aigym.domain.entity.WorkoutSession;
import com.aigym.dto.WorkoutSession.WorkoutLogRequest;
import com.aigym.dto.WorkoutSession.WorkoutSessionRequest;
import com.aigym.dto.WorkoutSession.WorkoutSessionResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ExerciseRepository;
import com.aigym.repository.WorkoutPlanRepository;
import com.aigym.repository.WorkoutSessionRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutSessionServiceImplTest {

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private WorkoutSessionServiceImpl workoutSessionService;

    private User currentUser;
    private WorkoutSession session;
    private WorkoutSessionRequest request;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").build();
        currentUser.setId(1L);

        session = new WorkoutSession();
        session.setId(1L);
        session.setUser(currentUser);
        session.setStartTime(LocalDateTime.now().minusHours(1));
        session.setEndTime(LocalDateTime.now());
        session.setTotalCaloriesBurned(300);

        request = new WorkoutSessionRequest();
        request.setStartTime(LocalDateTime.now().minusHours(1));
        request.setEndTime(LocalDateTime.now());
        request.setTotalCaloriesBurned(300);
    }

    @Test
    void createWorkoutSession_Success_WithoutPlan() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(genericMapper.mapToDto(any(WorkoutSession.class), eq(WorkoutSessionResponse.class))).thenReturn(new WorkoutSessionResponse());

        WorkoutSessionResponse res = workoutSessionService.createWorkoutSession(request);

        assertNotNull(res);
        verify(workoutSessionRepository, times(1)).save(any(WorkoutSession.class));
    }

    @Test
    void createWorkoutSession_Success_WithPlanAndLogs() {
        request.setWorkoutPlanId(1L);
        WorkoutLogRequest logReq = new WorkoutLogRequest();
        logReq.setExerciseId(10L);
        request.setLogs(List.of(logReq));

        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(1L);
        Exercise exercise = new Exercise();
        exercise.setId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workoutPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(exerciseRepository.findById(10L)).thenReturn(Optional.of(exercise));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(genericMapper.mapToDto(any(WorkoutSession.class), eq(WorkoutSessionResponse.class))).thenReturn(new WorkoutSessionResponse());

        WorkoutSessionResponse res = workoutSessionService.createWorkoutSession(request);

        assertNotNull(res);
        verify(workoutPlanRepository, times(1)).findById(1L);
        verify(exerciseRepository, times(1)).findById(10L);
    }

    @Test
    void getWorkoutSessionById_Success() {
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToDto(any(WorkoutSession.class), eq(WorkoutSessionResponse.class))).thenReturn(new WorkoutSessionResponse());

        WorkoutSessionResponse res = workoutSessionService.getWorkoutSessionById(1L);

        assertNotNull(res);
    }

    @Test
    void getWorkoutSessionById_Fail_NotOwner() {
        User otherUser = User.builder().build();
        otherUser.setId(2L);
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(BadRequestException.class, () -> workoutSessionService.getWorkoutSessionById(1L));
    }

    @Test
    void getMySessionHistory_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workoutSessionRepository.findByUserIdOrderByStartTimeDesc(1L)).thenReturn(List.of(session));
        when(genericMapper.mapToDto(any(WorkoutSession.class), eq(WorkoutSessionResponse.class))).thenReturn(new WorkoutSessionResponse());

        List<WorkoutSessionResponse> res = workoutSessionService.getMySessionHistory();

        assertFalse(res.isEmpty());
        assertEquals(1, res.size());
    }

    @Test
    void updateWorkoutSession_Success() {
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(genericMapper.mapToDto(any(WorkoutSession.class), eq(WorkoutSessionResponse.class))).thenReturn(new WorkoutSessionResponse());

        WorkoutSessionResponse res = workoutSessionService.updateWorkoutSession(1L, request);

        assertNotNull(res);
        verify(workoutSessionRepository, times(1)).save(session);
    }

    @Test
    void deleteWorkoutSession_Success() {
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertDoesNotThrow(() -> workoutSessionService.deleteWorkoutSession(1L));
        verify(workoutSessionRepository, times(1)).delete(session);
    }
}
