/**
 * THỰC THỂ: Thông tin thể chất người dùng (UserProfile)
 * Mô tả: Lưu các chỉ số cơ thể như chiều cao, cân nặng, cân nặng mục tiêu
 *         và lượng calo mục tiêu hàng ngày. Quan hệ 1-1 với User.
 * Bảng DB: user_profiles
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "user_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE user_profiles SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class UserProfile extends BaseEntity {

    // Quan hệ 1-1 với User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String avatarUrl; // Ảnh đại diện

    @Column(nullable = false)
    private Double height; // Chiều cao (cm)

    @Column(nullable = false)
    private Double weight; // Cân nặng hiện tại (kg)

    @Column(nullable = false)
    private Double targetWeight; // Cân nặng mục tiêu (kg)

    @Column(nullable = false)
    private Integer dailyCalorieGoal; // Mục tiêu calo mỗi ngày

    @Column
    private Integer dailyProteinGoal; // Mục tiêu protein (g)

    @Column
    private Integer dailyCarbsGoal; // Mục tiêu carbs (g)

    @Column
    private Integer dailyFatsGoal; // Mục tiêu fats (g)

    @Column
    private Integer dailyFiberGoal; // Mục tiêu chất xơ (g)

    @Column
    private java.time.LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column
    private com.aigym.domain.enums.Gender gender;

    @Enumerated(EnumType.STRING)
    @Column
    private com.aigym.domain.enums.ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column
    private com.aigym.domain.enums.GoalType goalType;
}
