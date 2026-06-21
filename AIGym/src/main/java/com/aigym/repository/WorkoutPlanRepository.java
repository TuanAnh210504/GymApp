package com.aigym.repository;

import com.aigym.domain.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    // --- Dùng cho READ: fetch toàn bộ cây dữ liệu bằng JOIN (tránh N+1 query) ---
    @EntityGraph(attributePaths = {
            "planDays",
            "planDays.planExercises",
            "planDays.planExercises.exercise"
    })
    List<WorkoutPlan> findWithDetailsByIsPublicTrue();

    @EntityGraph(attributePaths = {
            "planDays",
            "planDays.planExercises",
            "planDays.planExercises.exercise"
    })
    List<WorkoutPlan> findWithDetailsByCreatorId(Long creatorId);

    @EntityGraph(attributePaths = {
            "planDays",
            "planDays.planExercises",
            "planDays.planExercises.exercise"
    })
    Optional<WorkoutPlan> findWithDetailsById(Long id);

    // --- Dùng cho các tác vụ không cần toàn bộ cây con ---
    List<WorkoutPlan> findByIsPublicTrue();
    List<WorkoutPlan> findByCreatorId(Long creatorId);
    boolean existsByTitle(String title);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM workout_plans WHERE is_deleted = true", nativeQuery = true)
    List<WorkoutPlan> findAllDeletedNative();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE workout_plans SET is_deleted = false WHERE id = :id", nativeQuery = true)
    int restoreNative(@org.springframework.data.repository.query.Param("id") Long id);
}
