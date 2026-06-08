package com.aigym.dto.WeeklySchedule;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledExerciseRequest {

    @NotNull(message = "ID bài tập không được để trống")
    private Long exerciseId;

    private Integer targetSets;
    private Integer targetReps;
    private Double targetWeight;

    @NotNull(message = "Thứ tự bài tập không được để trống")
    private Integer orderIndex;

    private String note;
}
