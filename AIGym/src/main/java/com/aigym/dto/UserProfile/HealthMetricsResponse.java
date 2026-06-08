package com.aigym.dto.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthMetricsResponse {
    private Double bmi;
    private Double bmr;
    private Double tdee;
    private Integer dailyCalorieGoal;
}
