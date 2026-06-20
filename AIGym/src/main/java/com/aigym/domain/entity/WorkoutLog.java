/**
 * THỰC THỂ: Nhật ký bài tập (WorkoutLog)
 * Mô tả: Ghi lại chi tiết từng bài tập trong một buổi tập (WorkoutSession),
 *         bao gồm số hiệp (sets), số lần lặp (reps), tạ nâng (kg),
 *         thời gian nghỉ giữa hiệp (giây) và ghi chú.
 * Bảng DB: workout_logs
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "workout_logs")
@Getter
@Setter
@ToString(exclude = "session")
@EqualsAndHashCode(callSuper = true, exclude = "session")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE workout_logs SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class WorkoutLog extends BaseEntity {

    // Log này thuộc về buổi tập nào (Session)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    // Log này thuộc về bài tập nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer workoutsets;      // Số hiệp tập

    @Column(nullable = false)
    private Integer reps;             // Số lần mỗi hiệp

    private Double weight;            // Mức tạ (kg) - null nếu là bài tập bodyweight

    private Integer restTime;         // Thời gian nghỉ giữa hiệp (giây)

    @Column(columnDefinition = "TEXT")
    private String note;              // Ghi chú riêng
}