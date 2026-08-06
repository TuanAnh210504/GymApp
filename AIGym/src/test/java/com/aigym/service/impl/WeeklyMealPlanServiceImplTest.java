package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.*;
import com.aigym.domain.enums.DayOfWeek;
import com.aigym.domain.enums.MealType;
import com.aigym.domain.enums.Role;
import com.aigym.dto.MealPlan.MealPlanDayRequest;
import com.aigym.dto.MealPlan.PlannedMealRequest;
import com.aigym.dto.MealPlan.WeeklyMealPlanRequest;
import com.aigym.dto.MealPlan.WeeklyMealPlanResponse;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.FoodItemRepository;
import com.aigym.repository.NutritionLogRepository;
import com.aigym.repository.PlannedMealRepository;
import com.aigym.repository.WeeklyMealPlanRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyMealPlanServiceImplTest {

    @Mock
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private NutritionLogRepository nutritionLogRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private WeeklyMealPlanServiceImpl weeklyMealPlanService;

    private User currentUser;
    private WeeklyMealPlan plan;
    private WeeklyMealPlanRequest request;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").role(Role.USER).build();
        currentUser.setId(1L);

        plan = new WeeklyMealPlan();
        plan.setId(1L);
        plan.setName("My Meal Plan");
        plan.setUser(currentUser);
        plan.setActive(true);

        request = new WeeklyMealPlanRequest();
        request.setName("My Meal Plan");
        request.setActive(true);
    }

    @Test
    void createWeeklyMealPlan_Success_FromScratch() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyMealPlanRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.of(new WeeklyMealPlan()));
        when(weeklyMealPlanRepository.save(any(WeeklyMealPlan.class))).thenReturn(plan);
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyMealPlanResponse res = weeklyMealPlanService.createWeeklyMealPlan(request);

        assertNotNull(res);
        assertEquals("My Meal Plan", res.getName());
        verify(weeklyMealPlanRepository, times(2)).save(any(WeeklyMealPlan.class));
    }

    @Test
    void createWeeklyMealPlan_Success_AiCustomFood_CreateNew() {
        MealPlanDayRequest dayReq = new MealPlanDayRequest();
        dayReq.setDayOfWeek(DayOfWeek.MONDAY);
        dayReq.setLabel("Test Day");

        PlannedMealRequest mealReq = new PlannedMealRequest();
        mealReq.setCustomFoodName("New AI Food");
        mealReq.setAmount(100.0);
        mealReq.setMealType(MealType.LUNCH);
        dayReq.setPlannedMeals(List.of(mealReq));

        request.setMealPlanDays(List.of(dayReq));

        FoodItem newFood = new FoodItem();
        newFood.setName("New AI Food");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(weeklyMealPlanRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        when(foodItemRepository.findByNameIgnoreCaseAndIsPublicTrue("New AI Food")).thenReturn(Optional.empty());
        when(foodItemRepository.findByNameIgnoreCaseAndIsPublicFalseAndUserId("New AI Food", currentUser.getId())).thenReturn(Optional.empty());
        when(foodItemRepository.save(any(FoodItem.class))).thenReturn(newFood);
        when(weeklyMealPlanRepository.save(any(WeeklyMealPlan.class))).thenReturn(plan);
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyMealPlanResponse res = weeklyMealPlanService.createWeeklyMealPlan(request);

        assertNotNull(res);
        verify(foodItemRepository).findByNameIgnoreCaseAndIsPublicTrue("New AI Food");
        verify(foodItemRepository).findByNameIgnoreCaseAndIsPublicFalseAndUserId("New AI Food", currentUser.getId());
        verify(foodItemRepository).save(any(FoodItem.class));
    }

    @Test
    void getWeeklyMealPlanById_Success() {
        when(weeklyMealPlanRepository.findWithDetailsById(1L)).thenReturn(Optional.of(plan));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToDto(any(User.class), eq(com.aigym.dto.user.UserResponse.class))).thenReturn(new com.aigym.dto.user.UserResponse());

        WeeklyMealPlanResponse res = weeklyMealPlanService.getWeeklyMealPlanById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void markMealAsEaten_Success() {
        PlannedMeal meal = new PlannedMeal();
        meal.setId(10L);
        meal.setAmount(200.0);
        meal.setMealType(MealType.LUNCH);
        
        MealPlanDay day = new MealPlanDay();
        day.setWeeklyMealPlan(plan);
        meal.setMealPlanDay(day);

        FoodItem foodItem = new FoodItem();
        foodItem.setId(99L);
        meal.setFoodItem(foodItem);

        when(plannedMealRepository.findById(10L)).thenReturn(Optional.of(meal));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        
        NutritionLog savedLog = new NutritionLog();
        savedLog.setId(100L);
        when(nutritionLogRepository.save(any(NutritionLog.class))).thenReturn(savedLog);
        
        NutritionLogResponse logResponse = new NutritionLogResponse();
        when(genericMapper.mapToDto(any(NutritionLog.class), eq(NutritionLogResponse.class))).thenReturn(logResponse);

        NutritionLogResponse res = weeklyMealPlanService.markMealAsEaten(10L, 150.0);

        assertNotNull(res);
        assertTrue(meal.isEaten());
        verify(plannedMealRepository).save(meal);
        verify(nutritionLogRepository).save(argThat(log -> log.getAmount() == 150.0));
    }

    @Test
    void markMealAsEaten_Fail_NoFoodItem() {
        PlannedMeal meal = new PlannedMeal();
        meal.setId(10L);
        
        MealPlanDay day = new MealPlanDay();
        day.setWeeklyMealPlan(plan);
        meal.setMealPlanDay(day);
        
        // No FoodItem set

        when(plannedMealRepository.findById(10L)).thenReturn(Optional.of(meal));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertThrows(BadRequestException.class, () -> weeklyMealPlanService.markMealAsEaten(10L, 150.0));
    }
}
