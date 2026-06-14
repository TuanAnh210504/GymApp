package com.aigym.service.impl;

import com.aigym.domain.entity.UserProfile;
import com.aigym.domain.enums.ActivityLevel;
import com.aigym.domain.enums.Gender;
import com.aigym.domain.enums.GoalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HealthCalculatorServiceImplTest {

    private HealthCalculatorServiceImpl healthCalculatorService;

    @BeforeEach
    void setUp() {
        healthCalculatorService = new HealthCalculatorServiceImpl();
    }

    @Test
    void calculateAge_ValidDates() {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        LocalDate current = LocalDate.of(2020, 1, 1);
        assertEquals(30, healthCalculatorService.calculateAge(dob, current));
    }

    @Test
    void calculateAge_NullDates() {
        assertEquals(25, healthCalculatorService.calculateAge(null, LocalDate.now()));
    }

    @Test
    void calculateBMI_ValidInputs() {
        // 70kg, 175cm = 1.75m -> 70 / (1.75*1.75) = 22.857
        Double bmi = healthCalculatorService.calculateBMI(70.0, 175.0);
        assertEquals(22.857, bmi, 0.001);
    }

    @Test
    void calculateBMI_InvalidInputs() {
        assertEquals(0.0, healthCalculatorService.calculateBMI(0.0, 175.0));
        assertEquals(0.0, healthCalculatorService.calculateBMI(70.0, 0.0));
        assertEquals(0.0, healthCalculatorService.calculateBMI(null, 175.0));
    }

    @Test
    void calculateBMR_Male() {
        // 70kg, 175cm, 30 age -> (10*70) + (6.25*175) - (5*30) + 5 = 700 + 1093.75 - 150 + 5 = 1648.75
        Double bmr = healthCalculatorService.calculateBMR(70.0, 175.0, 30, Gender.MALE);
        assertEquals(1648.75, bmr, 0.01);
    }

    @Test
    void calculateBMR_Female() {
        // 60kg, 160cm, 25 age -> (10*60) + (6.25*160) - (5*25) - 161 = 600 + 1000 - 125 - 161 = 1314.0
        Double bmr = healthCalculatorService.calculateBMR(60.0, 160.0, 25, Gender.FEMALE);
        assertEquals(1314.0, bmr, 0.01);
    }

    @Test
    void calculateBMR_InvalidInputs() {
        assertEquals(0.0, healthCalculatorService.calculateBMR(null, 175.0, 30, Gender.MALE));
    }

    @Test
    void calculateTDEE_Sedentary() {
        Double tdee = healthCalculatorService.calculateTDEE(1000.0, ActivityLevel.SEDENTARY);
        assertEquals(1200.0, tdee, 0.01);
    }

    @Test
    void calculateTDEE_ExtraActive() {
        Double tdee = healthCalculatorService.calculateTDEE(1000.0, ActivityLevel.EXTRA_ACTIVE);
        assertEquals(1900.0, tdee, 0.01);
    }

    @Test
    void calculateTDEE_InvalidInputs() {
        assertEquals(0.0, healthCalculatorService.calculateTDEE(0.0, ActivityLevel.SEDENTARY));
    }

    @Test
    void calculateDailyCalorieGoal_LoseWeight() {
        Integer goal = healthCalculatorService.calculateDailyCalorieGoal(2500.0, GoalType.LOSE_WEIGHT, Gender.MALE);
        assertEquals(2000, goal);
    }

    @Test
    void calculateDailyCalorieGoal_BuildMuscle() {
        Integer goal = healthCalculatorService.calculateDailyCalorieGoal(2500.0, GoalType.BUILD_MUSCLE, Gender.MALE);
        assertEquals(2800, goal);
    }

    @Test
    void calculateDailyCalorieGoal_MinLimit() {
        Integer goalMale = healthCalculatorService.calculateDailyCalorieGoal(1000.0, GoalType.LOSE_WEIGHT, Gender.MALE);
        assertEquals(1500, goalMale); // Min for male is 1500

        Integer goalFemale = healthCalculatorService.calculateDailyCalorieGoal(1000.0, GoalType.LOSE_WEIGHT, Gender.FEMALE);
        assertEquals(1200, goalFemale); // Min for female is 1200
    }

    @Test
    void calculateDailyCalorieGoal_FromProfile() {
        UserProfile profile = new UserProfile();
        profile.setWeight(70.0);
        profile.setHeight(175.0);
        profile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        profile.setGender(Gender.MALE);
        profile.setActivityLevel(ActivityLevel.SEDENTARY);
        profile.setGoalType(GoalType.MAINTAIN_WEIGHT);

        Integer goal = healthCalculatorService.calculateDailyCalorieGoal(profile);
        assertTrue(goal > 1500);
    }

    @Test
    void calculateDailyCalorieGoal_FromProfile_Null() {
        assertThrows(IllegalArgumentException.class, () -> healthCalculatorService.calculateDailyCalorieGoal(null));
    }
}
