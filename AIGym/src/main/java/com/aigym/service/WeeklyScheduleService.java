package com.aigym.service;

import com.aigym.dto.WeeklySchedule.WeeklyScheduleRequest;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;

import java.util.List;

public interface WeeklyScheduleService {

    WeeklyScheduleResponse createWeeklySchedule(WeeklyScheduleRequest request);

    WeeklyScheduleResponse getWeeklyScheduleById(Long id);

    List<WeeklyScheduleResponse> getMyWeeklySchedules();

    WeeklyScheduleResponse getMyActiveSchedule();

    WeeklyScheduleResponse updateWeeklySchedule(Long id, WeeklyScheduleRequest request);

    WeeklyScheduleResponse setActiveSchedule(Long id);

    void deleteWeeklySchedule(Long id);
}
