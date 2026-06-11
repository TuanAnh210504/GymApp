package com.aigym.repository;

import com.aigym.domain.entity.WorkoutPlanDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutPlanDayRepository extends JpaRepository<WorkoutPlanDay, Long> {

    @Modifying
    @Query(value = "DELETE FROM workout_plan_exercises WHERE workout_plan_day_id IN " +
                   "(SELECT id FROM workout_plan_days WHERE workout_plan_id = :planId)", nativeQuery = true)
    void deleteExercisesByPlanIdNative(@Param("planId") Long planId);

    @Modifying
    @Query(value = "DELETE FROM workout_plan_days WHERE workout_plan_id = :planId", nativeQuery = true)
    void deleteDaysByPlanIdNative(@Param("planId") Long planId);
}
