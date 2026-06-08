package com.aigym.dto.WeeklySchedule;

import com.aigym.dto.Exercise.ExerciseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledExerciseResponse {
    private Long id;
    private ExerciseResponse exercise; // Cần trả về cả thông tin bài tập (tên, hình ảnh...)
    private Integer targetSets;
    private Integer targetReps;
    private Double targetWeight;
    private Integer orderIndex;
    private String note;
}
