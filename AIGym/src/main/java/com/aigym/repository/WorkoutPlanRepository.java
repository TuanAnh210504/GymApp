package com.aigym.repository;

import com.aigym.domain.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByIsPublicTrue();
    List<WorkoutPlan> findByCreatorId(Long creatorId);
    boolean existsByTitle(String title);
}
