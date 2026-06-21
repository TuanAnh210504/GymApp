package com.aigym.dto.MealPlan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMealPlanRequest {

    @NotBlank(message = "Tên thực đơn không được để trống")
    private String name;

    private String description;

    private boolean active = false;

    @Valid
    private List<MealPlanDayRequest> mealPlanDays = new ArrayList<>();
}
