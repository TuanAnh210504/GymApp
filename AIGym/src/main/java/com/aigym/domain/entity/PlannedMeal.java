/**
 * THỰC THỂ: Món ăn được xếp trong kế hoạch (PlannedMeal)
 * Mô tả: Một món ăn cụ thể được xếp vào một ngày trong thực đơn tuần.
 * Bảng DB: planned_meals
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "planned_meals")
@Getter
@Setter
@ToString(exclude = "mealPlanDay")
@EqualsAndHashCode(callSuper = true, exclude = "mealPlanDay")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedMeal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_day_id", nullable = false)
    private MealPlanDay mealPlanDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = true)
    private FoodItem foodItem;

    @Column(nullable = true)
    private String customFoodName; // Tên món AI gợi ý (nếu chưa map được với FoodItem)

    @Column(nullable = false)
    private Double amount; // Số gram

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType; // Bữa ăn nào

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú

    @Column(nullable = false)
    @Builder.Default
    private boolean isEaten = false; // Đã ăn chưa
}
