package com.aigym.service;

import com.aigym.dto.WorkoutPlan.WorkoutPlanRequest;
import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;

import java.util.List;

public interface WorkoutPlanService {

    WorkoutPlanResponse createWorkoutPlan(WorkoutPlanRequest request);

    WorkoutPlanResponse getWorkoutPlanById(Long id);

    List<WorkoutPlanResponse> getPublicWorkoutPlans();

    List<WorkoutPlanResponse> getMyWorkoutPlans();

    WorkoutPlanResponse updateWorkoutPlan(Long id, WorkoutPlanRequest request);

    void deleteWorkoutPlan(Long id);

    void bulkDeleteWorkoutPlans(List<Long> ids);

    List<WorkoutPlanResponse> getDeletedWorkoutPlans();

    void restoreWorkoutPlan(Long id);
}
