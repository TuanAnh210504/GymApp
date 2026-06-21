package com.aigym.dto.MealPlan;

import com.aigym.domain.enums.MealType;
import com.aigym.dto.FoodItem.FoodItemResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannedMealResponse {
    private Long id;
    private FoodItemResponse foodItem;
    private String customFoodName;
    private Double amount;
    private MealType mealType;
    private Integer orderIndex;
    private String note;
    private boolean eaten;
}
