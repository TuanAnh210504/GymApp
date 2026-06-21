package com.aigym.dto.UserProfile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @NotNull(message = "Chiều cao không được để trống")
    @Min(value = 50, message = "Chiều cao không hợp lệ")
    private Double height; // cm

    @NotNull(message = "Cân nặng hiện tại không được để trống")
    @Min(value = 20, message = "Cân nặng không hợp lệ")
    private Double weight; // kg

    @NotNull(message = "Cân nặng mục tiêu không được để trống")
    @Min(value = 20, message = "Cân nặng mục tiêu không hợp lệ")
    private Double targetWeight; // kg

    private Integer dailyCalorieGoal;
    private Integer dailyProteinGoal;
    private Integer dailyCarbsGoal;
    private Integer dailyFatsGoal;

    private java.time.LocalDate dateOfBirth;
    private com.aigym.domain.enums.Gender gender;
    private com.aigym.domain.enums.ActivityLevel activityLevel;
    private com.aigym.domain.enums.GoalType goalType;
}
