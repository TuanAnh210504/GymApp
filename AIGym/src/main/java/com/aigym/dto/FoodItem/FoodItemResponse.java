package com.aigym.dto.FoodItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {
    private Long id;
    private String name;
    private String brand;
    private Integer caloriesPer100g;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
}
