/**
 * THỰC THỂ: Bài tập đã xếp lịch (ScheduledExercise)
 * Mô tả: Một bài tập cụ thể được người dùng xếp vào một ngày trong lịch tuần.
 *         Lưu chỉ tiêu kế hoạch (số hiệp, số reps, tạ mục tiêu) để người dùng
 *         biết cần tập gì và bao nhiêu trong buổi hôm đó.
 * Bảng DB: scheduled_exercises
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scheduled_exercises")
@Getter
@Setter
@ToString(exclude = "scheduleDay")
@EqualsAndHashCode(callSuper = true, exclude = "scheduleDay")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledExercise extends BaseEntity {

    // Thuộc ngày tập nào trong lịch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_day_id", nullable = false)
    private ScheduleDay scheduleDay;

    // Bài tập nào (từ thư viện)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Exercise exercise;

    // Chỉ tiêu kế hoạch (mục tiêu - có thể null nếu chưa xác định)
    private Integer targetSets;   // Số hiệp mục tiêu
    private Integer targetReps;   // Số lần lặp mục tiêu mỗi hiệp
    private Double  targetWeight; // Tạ mục tiêu (kg) - null nếu bodyweight

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0; // Thứ tự bài trong ngày (dùng để kéo thả sắp xếp)

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú kế hoạch (VD: "Tăng 2.5kg so với tuần trước")
}
