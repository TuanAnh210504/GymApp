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

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM workout_plans WHERE is_deleted = true", nativeQuery = true)
    List<WorkoutPlan> findAllDeletedNative();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE workout_plans SET is_deleted = false WHERE id = :id", nativeQuery = true)
    int restoreNative(@org.springframework.data.repository.query.Param("id") Long id);
}
