package com.aigym.dto.WeeklySchedule;

import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyScheduleResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
    private UserResponse user;
    private List<ScheduleDayResponse> scheduleDays;
    private LocalDateTime createdAt;
}
