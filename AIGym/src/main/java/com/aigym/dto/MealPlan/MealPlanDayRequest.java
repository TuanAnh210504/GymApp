package com.aigym.dto.MealPlan;

import com.aigym.domain.enums.DayOfWeek;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDayRequest {

    @NotNull(message = "Thứ trong tuần không được để trống")
    private DayOfWeek dayOfWeek;

    @NotBlank(message = "Label không được để trống")
    private String label;

    private Boolean restDay = Boolean.FALSE;

    @Valid
    private List<PlannedMealRequest> plannedMeals = new ArrayList<>();
}
