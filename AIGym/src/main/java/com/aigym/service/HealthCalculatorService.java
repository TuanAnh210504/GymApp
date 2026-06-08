package com.aigym.service;

import com.aigym.domain.entity.UserProfile;
import com.aigym.domain.enums.ActivityLevel;
import com.aigym.domain.enums.Gender;
import com.aigym.domain.enums.GoalType;

import java.time.LocalDate;

public interface HealthCalculatorService {
    Integer calculateAge(LocalDate dateOfBirth, LocalDate currentDate);
    Double calculateBMI(Double weightKg, Double heightCm);
    Double calculateBMR(Double weightKg, Double heightCm, Integer age, Gender gender);
    Double calculateTDEE(Double bmr, ActivityLevel activityLevel);
    Integer calculateDailyCalorieGoal(Double tdee, GoalType goalType, Gender gender);
    Integer calculateDailyCalorieGoal(UserProfile profile);
}
