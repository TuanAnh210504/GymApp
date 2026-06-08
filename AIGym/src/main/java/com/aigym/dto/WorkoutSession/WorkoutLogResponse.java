package com.aigym.dto.WorkoutSession;

import com.aigym.dto.Exercise.ExerciseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLogResponse {
    private Long id;
    private ExerciseResponse exercise; // Trả về đầy đủ thông tin bài tập
    private Integer workoutsets;
    private Integer reps;
    private Double weight;
    private Integer restTime;
    private String note;
}
