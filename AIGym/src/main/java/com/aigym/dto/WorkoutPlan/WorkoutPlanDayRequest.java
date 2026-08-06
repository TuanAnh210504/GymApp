package com.aigym.dto.WorkoutPlan;

import com.aigym.domain.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanDayRequest {
    @NotNull(message = "Ngày trong tuần không được để trống")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Nhãn không được để trống")
    private String label;

    private Boolean restDay;

    private List<WorkoutPlanExerciseRequest> planExercises;
}
