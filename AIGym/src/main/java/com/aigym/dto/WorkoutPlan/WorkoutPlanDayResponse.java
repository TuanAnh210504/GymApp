package com.aigym.dto.WorkoutPlan;

import com.aigym.domain.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanDayResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private String label;
    private Boolean restDay;
    private List<WorkoutPlanExerciseResponse> planExercises;
}
