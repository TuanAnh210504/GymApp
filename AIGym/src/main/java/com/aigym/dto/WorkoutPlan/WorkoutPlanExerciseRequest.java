package com.aigym.dto.WorkoutPlan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanExerciseRequest {
    @NotNull(message = "ID bài tập không được để trống")
    private Long exerciseId;

    @Min(value = 1, message = "Số hiệp phải lớn hơn 0")
    private Integer targetSets;

    @Min(value = 1, message = "Số lần lặp phải lớn hơn 0")
    private Integer targetReps;

    private Double targetWeight; // Nullable

    @NotNull(message = "Thứ tự không được để trống")
    private Integer orderIndex;

    private String note;
}
