package com.aigym.service.impl;

import com.aigym.domain.entity.UserProfile;
import com.aigym.domain.enums.ActivityLevel;
import com.aigym.domain.enums.Gender;
import com.aigym.domain.enums.GoalType;
import com.aigym.service.HealthCalculatorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class HealthCalculatorServiceImpl implements HealthCalculatorService {

    @Override
    public Integer calculateAge(LocalDate dateOfBirth, LocalDate currentDate) {
        if (dateOfBirth == null) return 25; // Default age if missing
        if (currentDate == null) currentDate = LocalDate.now();
        int age = Period.between(dateOfBirth, currentDate).getYears();
        return Math.max(age, 1); // Age should be at least 1
    }

    @Override
    public Double calculateBMI(Double weightKg, Double heightCm) {
        if (weightKg == null || heightCm == null || weightKg <= 0 || heightCm <= 0) return 0.0;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    @Override
    public Double calculateBMR(Double weightKg, Double heightCm, Integer age, Gender gender) {
        if (weightKg == null || heightCm == null || age == null || weightKg <= 0 || heightCm <= 0 || age <= 0) return 0.0;
        
        Gender safeGender = (gender != null) ? gender : Gender.OTHER;
        
        // Mifflin-St Jeor Equation
        double bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        if (safeGender == Gender.MALE) {
            bmr += 5;
        } else if (safeGender == Gender.FEMALE) {
            bmr -= 161;
        } else {
            // Average or OTHER
            bmr -= 78;
        }
        return Math.max(bmr, 0.0);
    }

    @Override
    public Double calculateTDEE(Double bmr, ActivityLevel activityLevel) {
        if (bmr == null || bmr <= 0) return 0.0;
        
        ActivityLevel safeActivityLevel = (activityLevel != null) ? activityLevel : ActivityLevel.SEDENTARY;
        double multiplier = 1.2; // Default to SEDENTARY
        switch (safeActivityLevel) {
            case SEDENTARY:
                multiplier = 1.2;
                break;
            case LIGHTLY_ACTIVE:
                multiplier = 1.375;
                break;
            case MODERATELY_ACTIVE:
                multiplier = 1.55;
                break;
            case VERY_ACTIVE:
                multiplier = 1.725;
                break;
            case EXTRA_ACTIVE:
                multiplier = 1.9;
                break;
        }
        return bmr * multiplier;
    }

    @Override
    public Integer calculateDailyCalorieGoal(Double tdee, GoalType goalType, Gender gender) {
        if (tdee == null || tdee <= 0) return 2000;
        
        GoalType safeGoalType = (goalType != null) ? goalType : GoalType.MAINTAIN_WEIGHT;
        Gender safeGender = (gender != null) ? gender : Gender.OTHER;
        
        double goal = tdee;
        switch (safeGoalType) {
            case LOSE_WEIGHT:
                goal -= 500;
                break;
            case MAINTAIN_WEIGHT:
                // No change
                break;
            case BUILD_MUSCLE:
                goal += 300;
                break;
        }
        
        // Ensure a safe minimum based on Gender
        int minCalories = (safeGender == Gender.MALE) ? 1500 : 1200;
        if (goal < minCalories) {
            goal = minCalories;
        }
        return (int) Math.round(goal);
    }
    
    @Override
    public Integer calculateDailyCalorieGoal(UserProfile profile) {
        if (profile == null) throw new IllegalArgumentException("UserProfile cannot be null");
        
        int age = calculateAge(profile.getDateOfBirth(), LocalDate.now());
        double bmr = calculateBMR(profile.getWeight(), profile.getHeight(), age, profile.getGender());
        double tdee = calculateTDEE(bmr, profile.getActivityLevel());
        return calculateDailyCalorieGoal(tdee, profile.getGoalType(), profile.getGender());
    }
}
