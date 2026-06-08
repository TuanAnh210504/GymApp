package com.aigym.dto.NutritionLog;

import com.aigym.domain.enums.MealType;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionLogResponse {
    private Long id;
    private UserResponse user;
    private FoodItemResponse foodItem;
    private Double amount;
    private MealType mealType;
    private LocalDate loggedAt;
}
