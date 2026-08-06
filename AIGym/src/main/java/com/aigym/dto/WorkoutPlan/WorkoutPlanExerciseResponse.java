package com.aigym.dto.WorkoutPlan;

import com.aigym.dto.Exercise.ExerciseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanExerciseResponse {
    private Long id;
    private ExerciseResponse exercise; // Reuse ExerciseResponse
    private Integer targetSets;
    private Integer targetReps;
    private Double targetWeight;
    private Integer orderIndex;
    private String note;
}
