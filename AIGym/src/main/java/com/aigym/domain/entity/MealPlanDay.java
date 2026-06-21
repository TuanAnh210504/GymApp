/**
 * THỰC THỂ: Ngày trong thực đơn tuần (MealPlanDay)
 * Mô tả: Đại diện cho một thứ trong tuần (Thứ 2, Thứ 3...) bên trong một thực đơn.
 *         Một ngày có thể để trống (ngày cheat) hoặc có nhiều món ăn.
 * Bảng DB: meal_plan_days
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "meal_plan_days",
    uniqueConstraints = @UniqueConstraint(columnNames = {"weekly_meal_plan_id", "day_of_week"})
)
@Getter
@Setter
@ToString(exclude = {"weeklyMealPlan", "plannedMeals"})
@EqualsAndHashCode(callSuper = true, exclude = {"weeklyMealPlan", "plannedMeals"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanDay extends BaseEntity {

    // Thuộc thực đơn nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_meal_plan_id", nullable = false)
    private WeeklyMealPlan weeklyMealPlan;

    // Ngày trong tuần (Thứ 2 → Chủ nhật)
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRestDay = false; // Ngày cheat day / không lên kế hoạch

    // Danh sách món ăn được xếp vào ngày này
    @OneToMany(mappedBy = "mealPlanDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<PlannedMeal> plannedMeals = new ArrayList<>();
}
