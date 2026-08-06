package com.aigym.repository;

import com.aigym.domain.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {
    List<WorkoutSession> findByUserIdOrderByStartTimeDesc(Long userId);
    List<WorkoutSession> findByUserIdAndWorkoutPlanId(Long userId, Long workoutPlanId);
}
