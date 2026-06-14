package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.*;
import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.dto.WorkoutPlan.WorkoutPlanResponse;
import com.aigym.dto.WorkoutSession.WorkoutLogResponse;
import com.aigym.dto.WorkoutSession.WorkoutSessionRequest;
import com.aigym.dto.WorkoutSession.WorkoutSessionResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ExerciseRepository;
import com.aigym.repository.WorkoutPlanRepository;
import com.aigym.repository.WorkoutSessionRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.WorkoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutSessionServiceImpl implements WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        WorkoutSession session = new WorkoutSession();
        session.setUser(currentUser);
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setTotalCaloriesBurned(request.getTotalCaloriesBurned());
        session.setNotes(request.getNotes());

        // Gắn WorkoutPlan nếu có
        if (request.getWorkoutPlanId() != null) {
            WorkoutPlan plan = workoutPlanRepository.findById(request.getWorkoutPlanId())
                    .orElseThrow(() -> new NotFoundException(
                            "Không tìm thấy giáo án với ID: " + request.getWorkoutPlanId()));
            session.setWorkoutPlan(plan);
        }

        // Build danh sách WorkoutLog lồng nhau
        buildWorkoutLogs(session, request);

        WorkoutSession saved = workoutSessionRepository.save(session);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutSessionResponse getWorkoutSessionById(Long id) {
        WorkoutSession session = getSessionAndVerifyOwnership(id);
        return toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionResponse> getMySessionHistory() {
        User currentUser = currentUserService.getCurrentUser();
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdOrderByStartTimeDesc(currentUser.getId());
        return sessions.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public WorkoutSessionResponse updateWorkoutSession(Long id, WorkoutSessionRequest request) {
        WorkoutSession session = getSessionAndVerifyOwnership(id);

        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setTotalCaloriesBurned(request.getTotalCaloriesBurned());
        session.setNotes(request.getNotes());

        // Cập nhật WorkoutPlan
        if (request.getWorkoutPlanId() != null) {
            WorkoutPlan plan = workoutPlanRepository.findById(request.getWorkoutPlanId())
                    .orElseThrow(() -> new NotFoundException(
                            "Không tìm thấy giáo án với ID: " + request.getWorkoutPlanId()));
            session.setWorkoutPlan(plan);
        } else {
            session.setWorkoutPlan(null);
        }

        // Clear danh sách logs cũ → orphanRemoval sẽ tự xoá khỏi DB
        session.getLogs().clear();

        // Build lại danh sách logs mới
        buildWorkoutLogs(session, request);

        WorkoutSession updated = workoutSessionRepository.save(session);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorkoutSession(Long id) {
        WorkoutSession session = getSessionAndVerifyOwnership(id);
        workoutSessionRepository.delete(session);
    }

    private void buildWorkoutLogs(WorkoutSession session, WorkoutSessionRequest request) {
        if (request.getLogs() != null) {
            request.getLogs().forEach(logReq -> {
                Exercise exercise = exerciseRepository.findById(logReq.getExerciseId())
                        .orElseThrow(
                                () -> new NotFoundException("Không tìm thấy bài tập ID: " + logReq.getExerciseId()));

                WorkoutLog log = new WorkoutLog();
                log.setSession(session); // Quan hệ 2 chiều
                log.setExercise(exercise);
                log.setWorkoutsets(logReq.getWorkoutsets());
                log.setReps(logReq.getReps());
                log.setWeight(logReq.getWeight());
                log.setRestTime(logReq.getRestTime());
                log.setNote(logReq.getNote());

                session.getLogs().add(log);
            });
        }
    }

    private WorkoutSession getSessionAndVerifyOwnership(Long id) {
        WorkoutSession session = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy buổi tập với ID: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập buổi tập này");
        }
        return session;
    }

    private WorkoutSessionResponse toResponse(WorkoutSession session) {
        WorkoutSessionResponse response = genericMapper.mapToDto(session, WorkoutSessionResponse.class);

        if (session.getWorkoutPlan() != null) {
            response.setWorkoutPlan(genericMapper.mapToDto(session.getWorkoutPlan(), WorkoutPlanResponse.class));
        }

        // Map danh sách WorkoutLog lồng nhau
        List<WorkoutLogResponse> logResponses = new ArrayList<>();
        if (session.getLogs() != null) {
            session.getLogs().forEach(log -> {
                WorkoutLogResponse logResp = genericMapper.mapToDto(log, WorkoutLogResponse.class);
                if (log.getExercise() != null) {
                    logResp.setExercise(genericMapper.mapToDto(log.getExercise(), ExerciseResponse.class));
                }
                logResponses.add(logResp);
            });
        }
        response.setLogs(logResponses);
        return response;
    }
}
