package com.aigym.service;

import com.aigym.dto.WorkoutSession.WorkoutSessionRequest;
import com.aigym.dto.WorkoutSession.WorkoutSessionResponse;

import java.util.List;

public interface WorkoutSessionService {

    WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest request);

    WorkoutSessionResponse getWorkoutSessionById(Long id);

    List<WorkoutSessionResponse> getMySessionHistory();

    WorkoutSessionResponse updateWorkoutSession(Long id, WorkoutSessionRequest request);

    void deleteWorkoutSession(Long id);
}
