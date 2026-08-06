package com.aigym.service;

import com.aigym.dto.MealPlan.WeeklyMealPlanRequest;
import com.aigym.dto.MealPlan.WeeklyMealPlanResponse;
import com.aigym.dto.NutritionLog.NutritionLogResponse;

import java.util.List;

public interface WeeklyMealPlanService {

    WeeklyMealPlanResponse createWeeklyMealPlan(WeeklyMealPlanRequest request);

    WeeklyMealPlanResponse getWeeklyMealPlanById(Long id);

    List<WeeklyMealPlanResponse> getMyWeeklyMealPlans();

    WeeklyMealPlanResponse getMyActiveMealPlan();

    WeeklyMealPlanResponse updateWeeklyMealPlan(Long id, WeeklyMealPlanRequest request);

    WeeklyMealPlanResponse setActiveMealPlan(Long id);

    void deleteWeeklyMealPlan(Long id);

    NutritionLogResponse markMealAsEaten(Long mealId, Double actualAmount);
    
    void unmarkMealAsEaten(Long mealId);

    com.aigym.dto.MealPlan.PlannedMealResponse updateMealAmount(Long mealId, Double newAmount);
}
