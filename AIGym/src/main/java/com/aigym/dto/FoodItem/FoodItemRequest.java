package com.aigym.dto.FoodItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemRequest {

    @NotBlank(message = "Tên thực phẩm không được để trống")
    private String name;

    private String brand;

    @NotNull(message = "Lượng calo không được để trống")
    @Min(value = 0, message = "Lượng calo phải >= 0")
    private Integer caloriesPer100g;

    @NotNull(message = "Lượng protein không được để trống")
    @Min(value = 0, message = "Lượng protein phải >= 0")
    private Double protein;

    @NotNull(message = "Lượng carbs không được để trống")
    @Min(value = 0, message = "Lượng carbs phải >= 0")
    private Double carbs;

    @NotNull(message = "Lượng chất béo không được để trống")
    @Min(value = 0, message = "Lượng chất béo phải >= 0")
    private Double fat;

    @Min(value = 0, message = "Lượng chất xơ phải >= 0")
    private Double fiber;
}
