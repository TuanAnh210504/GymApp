package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.User;
import com.aigym.domain.entity.WorkoutPlan;
import com.aigym.dto.WorkoutPlan.WorkoutPlanRequest;
import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.WorkoutPlanRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.WorkoutPlanService;
import com.aigym.domain.entity.WorkoutPlanDay;
import com.aigym.domain.entity.WorkoutPlanExercise;
import com.aigym.domain.entity.Exercise;
import com.aigym.domain.enums.Role;
import com.aigym.dto.WorkoutPlan.WorkoutPlanDayResponse;
import com.aigym.dto.WorkoutPlan.WorkoutPlanExerciseResponse;
import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.repository.WorkoutPlanDayRepository;
import com.aigym.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutPlanDayRepository workoutPlanDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(WorkoutPlanRequest request) {
        if (workoutPlanRepository.existsByTitle(request.getTitle())) {
            throw new BadRequestException("Giáo án với tên '" + request.getTitle() + "' đã tồn tại");
        }

        User currentUser = currentUserService.getCurrentUser();
        WorkoutPlan plan = genericMapper.mapToEntity(request, WorkoutPlan.class);
        plan.setCreator(currentUser);

        buildPlanHierarchy(plan, request);

        WorkoutPlan saved = workoutPlanRepository.save(plan);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanResponse getWorkoutPlanById(Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giáo án với ID: " + id));
        return toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getPublicWorkoutPlans() {
        List<WorkoutPlan> plans = workoutPlanRepository.findByIsPublicTrue();
        return plans.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyWorkoutPlans() {
        User currentUser = currentUserService.getCurrentUser();
        List<WorkoutPlan> plans = workoutPlanRepository.findByCreatorId(currentUser.getId());
        return plans.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(Long id, WorkoutPlanRequest request) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giáo án với ID: " + id));

        // Kiểm tra quyền sở hữu (chỉ người tạo mới được sửa, hoặc là ADMIN)
        User currentUser = currentUserService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCreator = plan.getCreator() != null && plan.getCreator().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator) {
            throw new BadRequestException("Bạn không có quyền sửa giáo án này");
        }

        if (!plan.getTitle().equals(request.getTitle()) && workoutPlanRepository.existsByTitle(request.getTitle())) {
            throw new BadRequestException("Giáo án với tên '" + request.getTitle() + "' đã tồn tại");
        }

        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setDurationWeeks(request.getDurationWeeks());
        plan.setDifficulty(request.getDifficulty());
        plan.setPublic(request.isPublic());

        workoutPlanDayRepository.deleteExercisesByPlanIdNative(id);
        workoutPlanDayRepository.deleteDaysByPlanIdNative(id);
        
        plan.getPlanDays().clear();

        buildPlanHierarchy(plan, request);

        WorkoutPlan updated = workoutPlanRepository.save(plan);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorkoutPlan(Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giáo án với ID: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!plan.getCreator().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền xoá giáo án này");
        }

        workoutPlanRepository.delete(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getDeletedWorkoutPlans() {
        List<WorkoutPlan> plans = workoutPlanRepository.findAllDeletedNative();
        return plans.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void restoreWorkoutPlan(Long id) {
        int updated = workoutPlanRepository.restoreNative(id);
        if (updated == 0) {
            throw new NotFoundException("Không tìm thấy giáo án đã xóa với ID: " + id);
        }
    }

    private WorkoutPlanResponse toResponse(WorkoutPlan plan) {
        WorkoutPlanResponse response = genericMapper.mapToDto(plan, WorkoutPlanResponse.class);

        List<WorkoutPlanDayResponse> dayResponses = new ArrayList<>();
        if (plan.getPlanDays() != null) {
            plan.getPlanDays().forEach(day -> {
                WorkoutPlanDayResponse dayResp = genericMapper.mapToDto(day, WorkoutPlanDayResponse.class);

                List<WorkoutPlanExerciseResponse> exResponses = new ArrayList<>();
                if (day.getPlanExercises() != null) {
                    day.getPlanExercises().forEach(ex -> {
                        WorkoutPlanExerciseResponse exResp = genericMapper.mapToDto(ex, WorkoutPlanExerciseResponse.class);
                        if (ex.getExercise() != null) {
                            exResp.setExercise(genericMapper.mapToDto(ex.getExercise(), ExerciseResponse.class));
                        }
                        exResponses.add(exResp);
                    });
                }
                dayResp.setPlanExercises(exResponses);
                dayResponses.add(dayResp);
            });
        }
        response.setPlanDays(dayResponses);
        return response;
    }

    private void buildPlanHierarchy(WorkoutPlan plan, WorkoutPlanRequest request) {
        if (request.getPlanDays() != null) {
            request.getPlanDays().forEach(dayReq -> {
                WorkoutPlanDay day = new WorkoutPlanDay();
                day.setWorkoutPlan(plan);
                day.setDayOfWeek(dayReq.getDayOfWeek());
                day.setLabel(dayReq.getLabel());
                day.setRestDay(dayReq.isRestDay());

                if (dayReq.getPlanExercises() != null) {
                    dayReq.getPlanExercises().forEach(exReq -> {
                        Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                                .orElseThrow(() -> new NotFoundException(
                                        "Không tìm thấy bài tập ID: " + exReq.getExerciseId()));

                        WorkoutPlanExercise planExercise = new WorkoutPlanExercise();
                        planExercise.setWorkoutPlanDay(day);
                        planExercise.setExercise(exercise);
                        planExercise.setTargetSets(exReq.getTargetSets());
                        planExercise.setTargetReps(exReq.getTargetReps());
                        planExercise.setTargetWeight(exReq.getTargetWeight());
                        planExercise.setOrderIndex(exReq.getOrderIndex());
                        planExercise.setNote(exReq.getNote());

                        day.getPlanExercises().add(planExercise);
                    });
                }
                plan.getPlanDays().add(day);
            });
        }
    }
}
