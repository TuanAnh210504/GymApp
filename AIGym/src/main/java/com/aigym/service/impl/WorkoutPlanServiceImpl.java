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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
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

        // Kiểm tra quyền sở hữu (chỉ người tạo mới được sửa)
        User currentUser = currentUserService.getCurrentUser();
        if (!plan.getCreator().getId().equals(currentUser.getId())) {
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

    private WorkoutPlanResponse toResponse(WorkoutPlan plan) {
        return genericMapper.mapToDto(plan, WorkoutPlanResponse.class);
    }
}
