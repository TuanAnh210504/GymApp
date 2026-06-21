/**
 * THỰC THỂ: Lịch ăn uống cá nhân theo tuần (WeeklyMealPlan)
 * Mô tả: Mỗi user có thể tạo nhiều lịch ăn uống (VD: "Thực đơn giảm cân", "Thực đơn siết mỡ").
 *         Chỉ một lịch được kích hoạt (isActive = true) tại một thời điểm.
 *         Lịch này chứa các ngày (MealPlanDay) được xếp vào các thứ trong tuần.
 * Bảng DB: weekly_meal_plans
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_meal_plans")
@Getter
@Setter
@ToString(exclude = "mealPlanDays")
@EqualsAndHashCode(callSuper = true, exclude = "mealPlanDays")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyMealPlan extends BaseEntity {

    // Chủ sở hữu thực đơn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name; // VD: "Thực đơn giảm cân tháng 6"

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả ngắn về lịch

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = false; // Chỉ 1 lịch active tại một thời điểm

    // Danh sách các ngày trong lịch
    @OneToMany(mappedBy = "weeklyMealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealPlanDay> mealPlanDays = new ArrayList<>();
}
