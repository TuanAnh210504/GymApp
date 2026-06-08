package com.aigym.dto.WeeklySchedule;

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
public class ScheduleDayRequest {

    @NotNull(message = "Ngày trong tuần không được để trống")
    private DayOfWeek dayOfWeek;

    @NotBlank(message = "Tên nhãn ngày (label) không được để trống")
    private String label;

    private boolean isRestDay = false;

    // Các bài tập lồng bên trong ngày này
    @Valid
    private List<ScheduledExerciseRequest> scheduledExercises = new ArrayList<>();
}
