/**
 * THỰC THỂ: Kế hoạch tập luyện (WorkoutPlan)
 * Mô tả: Lưu các giáo án tập luyện mẫu (VD: Push-Pull-Legs, Full Body).
 *         Admin tạo công khai (isPublic=true) hoặc User tự tạo giáo án riêng.
 * Bảng DB: workout_plans
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "workout_plans")
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE workout_plans SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class WorkoutPlan extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column
    private String coverImageUrl; // Ảnh bìa giáo án

    @Column(nullable = false)
    private Integer durationWeeks;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("isPublic")
    private Boolean isPublic = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User creator;

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WorkoutPlanDay> planDays = new LinkedHashSet<>();
}
