package com.aigym.dto.WorkoutSession;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSessionRequest {

    private Long workoutPlanId; // Tuỳ chọn - null nếu tập tự do

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalCaloriesBurned;

    private String notes;

    // Danh sách bài tập đã tập trong buổi này (lồng nhau)
    @Valid
    private List<WorkoutLogRequest> logs = new ArrayList<>();
}
