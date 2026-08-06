/**
 * THỰC THỂ: Nhật ký dinh dưỡng (NutritionLog)
 * Mô tả: Ghi lại lượng thực phẩm người dùng tiêu thụ trong ngày,
 *         phân loại theo bữa ăn (BREAKFAST, LUNCH, DINNER, SNACK).
 *         Hệ thống tính toán tổng calo dựa trên FoodItem và lượng ăn (gram).
 * Bảng DB: nutrition_logs
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "nutrition_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE nutrition_logs SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class NutritionLog extends BaseEntity {

    // Người dùng ghi nhật ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thực phẩm được ăn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(nullable = false)
    private Double amount; // Số gram ăn vào

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType; // Bữa ăn nào

    @Column(nullable = false)
    private LocalDate loggedAt; // Ngày ghi
}
