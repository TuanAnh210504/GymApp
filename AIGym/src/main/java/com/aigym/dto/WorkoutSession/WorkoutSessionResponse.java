package com.aigym.dto.WorkoutSession;

import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;
import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSessionResponse {
    private Long id;
    private UserResponse user;
    private WorkoutPlanResponse workoutPlan; // Có thể null nếu tập tự do
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCaloriesBurned;
    private String notes;
    private List<WorkoutLogResponse> logs;
    private LocalDateTime createdAt;
}
