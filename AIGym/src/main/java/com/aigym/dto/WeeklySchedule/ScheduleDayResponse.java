package com.aigym.dto.WeeklySchedule;

import com.aigym.domain.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDayResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private String label;
    private boolean isRestDay;
    private List<ScheduledExerciseResponse> scheduledExercises;
}
