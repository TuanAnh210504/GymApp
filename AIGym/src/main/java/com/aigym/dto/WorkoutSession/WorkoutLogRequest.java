package com.aigym.dto.WorkoutSession;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLogRequest {

    @NotNull(message = "ID bài tập không được để trống")
    private Long exerciseId;

    @NotNull(message = "Số hiệp không được để trống")
    @Min(value = 1, message = "Số hiệp phải >= 1")
    private Integer workoutsets;

    @NotNull(message = "Số lần lặp không được để trống")
    @Min(value = 1, message = "Số lần lặp phải >= 1")
    private Integer reps;

    @Min(value = 0, message = "Mức tạ phải >= 0")
    private Double weight; // null nếu bodyweight

    @Min(value = 0, message = "Thời gian nghỉ phải >= 0")
    private Integer restTime; // giây

    private String note;
}
