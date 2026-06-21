package com.aigym.dto.UserProfile;

import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private Double height;
    private Double weight;
    private Double targetWeight;
    private Integer dailyCalorieGoal;
    private Integer dailyProteinGoal;
    private Integer dailyCarbsGoal;
    private Integer dailyFatsGoal;
    private Integer dailyFiberGoal;
    
    private java.time.LocalDate dateOfBirth;
    private com.aigym.domain.enums.Gender gender;
    private com.aigym.domain.enums.ActivityLevel activityLevel;
    private com.aigym.domain.enums.GoalType goalType;
    
    // Trả về kèm thông tin cơ bản của user
    private UserResponse user;
}
