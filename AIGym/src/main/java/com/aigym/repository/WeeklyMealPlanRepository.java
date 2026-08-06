package com.aigym.repository;

import com.aigym.domain.entity.WeeklyMealPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyMealPlanRepository extends JpaRepository<WeeklyMealPlan, Long> {

    // --- Dùng cho READ (danh sách/chi tiết): fetch toàn bộ cây dữ liệu bằng JOIN, không bị N+1 ---
    @EntityGraph(attributePaths = {
            "mealPlanDays",
            "mealPlanDays.plannedMeals",
            "mealPlanDays.plannedMeals.foodItem"
    })
    List<WeeklyMealPlan> findWithDetailsByUserId(Long userId);

    @EntityGraph(attributePaths = {
            "mealPlanDays",
            "mealPlanDays.plannedMeals",
            "mealPlanDays.plannedMeals.foodItem"
    })
    Optional<WeeklyMealPlan> findWithDetailsById(Long id);

    // --- Dùng cho các tác vụ nhẹ (deactivate, check active) ---
    List<WeeklyMealPlan> findByUserId(Long userId);
    Optional<WeeklyMealPlan> findByUserIdAndIsActiveTrue(Long userId);

    @EntityGraph(attributePaths = {
            "mealPlanDays",
            "mealPlanDays.plannedMeals",
            "mealPlanDays.plannedMeals.foodItem"
    })
    Optional<WeeklyMealPlan> findWithDetailsByUserIdAndIsActiveTrue(Long userId);
}
