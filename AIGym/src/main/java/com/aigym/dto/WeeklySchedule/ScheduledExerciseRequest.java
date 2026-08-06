package com.aigym.dto.WeeklySchedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledExerciseRequest {

    // Bài tập từ thư viện - null nếu là bài AI tự chế
    private Long exerciseId;

    // Chi tiết bài tập do AI tự chế - null nếu dùng exerciseId
    private com.aigym.dto.Exercise.ExerciseRequest customExercise;

    private Integer targetSets;
    private Integer targetReps;
    private Double targetWeight;
    private Integer orderIndex;
    private String note;
}
