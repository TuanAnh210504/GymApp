package com.aigym.dto.WorkoutPlan;

import com.aigym.domain.enums.Difficulty;
import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanResponse {
    private Long id;
    private String title;
    private String description;
    private Integer durationWeeks;
    private Difficulty difficulty;
    private boolean isPublic;
    private UserResponse creator;
    private LocalDateTime createdAt;
}
