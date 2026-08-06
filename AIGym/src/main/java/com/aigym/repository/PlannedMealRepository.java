package com.aigym.repository;

import com.aigym.domain.entity.PlannedMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlannedMealRepository extends JpaRepository<PlannedMeal, Long> {

    // Hard-delete tất cả planned_meals thuộc các ngày của một meal plan (bypass orphanRemoval)
    @Modifying
    @Query(value = "DELETE FROM planned_meals WHERE meal_plan_day_id IN " +
            "(SELECT id FROM meal_plan_days WHERE weekly_meal_plan_id = :planId)",
            nativeQuery = true)
    void hardDeleteMealsByPlanId(@Param("planId") Long planId);

    // Hard-delete tất cả meal_plan_days của một meal plan
    @Modifying
    @Query(value = "DELETE FROM meal_plan_days WHERE weekly_meal_plan_id = :planId",
            nativeQuery = true)
    void hardDeleteDaysByPlanId(@Param("planId") Long planId);
}
