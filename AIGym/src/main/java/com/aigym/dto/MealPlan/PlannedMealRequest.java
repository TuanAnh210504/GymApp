package com.aigym.dto.MealPlan;

import com.aigym.domain.enums.MealType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannedMealRequest {

    private Long foodItemId; // Nullable if AI suggests a new food

    private String customFoodName; // Tên món AI gợi ý nếu chưa có trong DB

    @NotNull(message = "Số lượng không được để trống")
    private Double amount;

    @NotNull(message = "Loại bữa ăn không được để trống")
    private MealType mealType;

    private Integer orderIndex = 0;

    private String note;

    private Boolean eaten = Boolean.FALSE;

    // Các trường sau dùng cho AI tự tạo FoodItem mới
    private Integer caloriesPer100g;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
}
