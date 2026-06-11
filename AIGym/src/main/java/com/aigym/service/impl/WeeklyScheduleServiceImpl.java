package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.*;
import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.dto.WeeklySchedule.ScheduleDayResponse;
import com.aigym.dto.WeeklySchedule.ScheduledExerciseResponse;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleRequest;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ScheduleDayRepository;
import com.aigym.repository.ExerciseRepository;
import com.aigym.repository.WeeklyScheduleRepository;
import com.aigym.repository.WorkoutPlanRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.WeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklyScheduleServiceImpl implements WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public WeeklyScheduleResponse createWeeklySchedule(WeeklyScheduleRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        // 1. Tạo WeeklySchedule
        WeeklySchedule schedule = new WeeklySchedule();
        schedule.setUser(currentUser);
        schedule.setName(request.getName());
        schedule.setDescription(request.getDescription());
        schedule.setActive(request.isActive());

        // Nếu cái mới này được active, phải de-active cái cũ
        if (request.isActive()) {
            deactivateCurrentActiveSchedule(currentUser.getId());
        }

        if (request.getWorkoutPlanId() != null) {
            // Clone từ giáo án mẫu
            WorkoutPlan plan = workoutPlanRepository.findById(request.getWorkoutPlanId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy giáo án mẫu ID: " + request.getWorkoutPlanId()));
            
            if (plan.getPlanDays() != null) {
                plan.getPlanDays().forEach(planDay -> {
                    ScheduleDay day = new ScheduleDay();
                    day.setWeeklySchedule(schedule);
                    day.setDayOfWeek(planDay.getDayOfWeek());
                    day.setLabel(planDay.getLabel());
                    day.setRestDay(planDay.isRestDay());

                    if (planDay.getPlanExercises() != null) {
                        planDay.getPlanExercises().forEach(planEx -> {
                            ScheduledExercise ex = new ScheduledExercise();
                            ex.setScheduleDay(day);
                            ex.setExercise(planEx.getExercise());
                            ex.setTargetSets(planEx.getTargetSets());
                            ex.setTargetReps(planEx.getTargetReps());
                            ex.setTargetWeight(planEx.getTargetWeight());
                            ex.setOrderIndex(planEx.getOrderIndex());
                            ex.setNote(planEx.getNote());
                            day.getScheduledExercises().add(ex);
                        });
                    }
                    schedule.getScheduleDays().add(day);
                });
            }
        } else {
            // Build quan hệ lồng nhau từ request thông thường
            buildScheduleHierarchy(schedule, request);
        }

        // 3. Save (Cascade sẽ lưu toàn bộ days và exercises)
        WeeklySchedule saved = weeklyScheduleRepository.save(schedule);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyScheduleResponse getWeeklyScheduleById(Long id) {
        WeeklySchedule schedule = getScheduleAndVerifyOwnership(id);
        return toResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyScheduleResponse> getMyWeeklySchedules() {
        User currentUser = currentUserService.getCurrentUser();
        List<WeeklySchedule> schedules = weeklyScheduleRepository.findByUserId(currentUser.getId());
        return schedules.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyScheduleResponse getMyActiveSchedule() {
        User currentUser = currentUserService.getCurrentUser();
        WeeklySchedule schedule = weeklyScheduleRepository.findByUserIdAndIsActiveTrue(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Bạn chưa có lịch tập nào đang được kích hoạt"));
        return toResponse(schedule);
    }

    @Override
    @Transactional
    public WeeklyScheduleResponse updateWeeklySchedule(Long id, WeeklyScheduleRequest request) {
        WeeklySchedule schedule = getScheduleAndVerifyOwnership(id);

        schedule.setName(request.getName());
        schedule.setDescription(request.getDescription());

        if (request.isActive() && !schedule.isActive()) {
            deactivateCurrentActiveSchedule(schedule.getUser().getId());
        }
        schedule.setActive(request.isActive());

        // Hard-delete old days + exercises via native SQL to avoid soft-delete unique constraint conflict
        scheduleDayRepository.hardDeleteExercisesByScheduleId(id);
        scheduleDayRepository.hardDeleteDaysByScheduleId(id);
        // Evict the stale collection from session so Hibernate doesn't try to soft-delete again
        schedule.getScheduleDays().clear();

        // Build new hierarchy
        buildScheduleHierarchy(schedule, request);

        WeeklySchedule updated = weeklyScheduleRepository.save(schedule);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public WeeklyScheduleResponse setActiveSchedule(Long id) {
        WeeklySchedule schedule = getScheduleAndVerifyOwnership(id);

        if (!schedule.isActive()) {
            deactivateCurrentActiveSchedule(schedule.getUser().getId());
            schedule.setActive(true);
            weeklyScheduleRepository.save(schedule);
        }

        return toResponse(schedule);
    }

    @Override
    @Transactional
    public void deleteWeeklySchedule(Long id) {
        WeeklySchedule schedule = getScheduleAndVerifyOwnership(id);
        weeklyScheduleRepository.delete(schedule);
    }

    private void deactivateCurrentActiveSchedule(Long userId) {
        Optional<WeeklySchedule> activeSchedule = weeklyScheduleRepository.findByUserIdAndIsActiveTrue(userId);
        activeSchedule.ifPresent(s -> {
            s.setActive(false);
            weeklyScheduleRepository.save(s);
        });
    }

    private void buildScheduleHierarchy(WeeklySchedule schedule, WeeklyScheduleRequest request) {
        if (request.getScheduleDays() != null) {
            request.getScheduleDays().forEach(dayReq -> {
                ScheduleDay day = new ScheduleDay();
                day.setWeeklySchedule(schedule); // Quan hệ 2 chiều
                day.setDayOfWeek(dayReq.getDayOfWeek());
                day.setLabel(dayReq.getLabel());
                day.setRestDay(dayReq.isRestDay());

                if (dayReq.getScheduledExercises() != null) {
                    dayReq.getScheduledExercises().forEach(exReq -> {
                        Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                                .orElseThrow(() -> new NotFoundException(
                                        "Không tìm thấy bài tập ID: " + exReq.getExerciseId()));

                        ScheduledExercise scheduledExercise = new ScheduledExercise();
                        scheduledExercise.setScheduleDay(day); // Quan hệ 2 chiều
                        scheduledExercise.setExercise(exercise);
                        scheduledExercise.setTargetSets(exReq.getTargetSets());
                        scheduledExercise.setTargetReps(exReq.getTargetReps());
                        scheduledExercise.setTargetWeight(exReq.getTargetWeight());
                        scheduledExercise.setOrderIndex(exReq.getOrderIndex());
                        scheduledExercise.setNote(exReq.getNote());

                        day.getScheduledExercises().add(scheduledExercise);
                    });
                }
                schedule.getScheduleDays().add(day);
            });
        }
    }

    private WeeklySchedule getScheduleAndVerifyOwnership(Long id) {
        WeeklySchedule schedule = weeklyScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy lịch tập với ID: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!schedule.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập lịch tập này");
        }
        return schedule;
    }

    // Thủ công map response do cấu trúc lồng sâu 3 tầng
    private WeeklyScheduleResponse toResponse(WeeklySchedule schedule) {
        WeeklyScheduleResponse response = genericMapper.mapToDto(schedule, WeeklyScheduleResponse.class);

        List<ScheduleDayResponse> dayResponses = new ArrayList<>();
        if (schedule.getScheduleDays() != null) {
            schedule.getScheduleDays().forEach(day -> {
                ScheduleDayResponse dayResp = genericMapper.mapToDto(day, ScheduleDayResponse.class);

                List<ScheduledExerciseResponse> exResponses = new ArrayList<>();
                if (day.getScheduledExercises() != null) {
                    day.getScheduledExercises().forEach(ex -> {
                        ScheduledExerciseResponse exResp = genericMapper.mapToDto(ex, ScheduledExerciseResponse.class);
                        if (ex.getExercise() != null) {
                            exResp.setExercise(genericMapper.mapToDto(ex.getExercise(), ExerciseResponse.class));
                        }
                        exResponses.add(exResp);
                    });
                }
                dayResp.setScheduledExercises(exResponses);
                dayResponses.add(dayResp);
            });
        }
        response.setScheduleDays(dayResponses);
        return response;
    }
}
