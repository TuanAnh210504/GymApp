package com.aigym.dto.WeeklySchedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyScheduleRequest {

    @NotBlank(message = "Tên lịch tập không được để trống")
    private String name;

    private String description;

    private boolean isActive = false;

    // Các ngày tập lồng bên trong lịch tuần này
    @Valid
    private List<ScheduleDayRequest> scheduleDays = new ArrayList<>();
}
