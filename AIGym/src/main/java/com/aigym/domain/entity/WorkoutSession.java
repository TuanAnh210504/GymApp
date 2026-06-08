/**
 * THỰC THỂ: Buổi tập luyện (WorkoutSession)
 * Mô tả: Ghi lại thông tin một buổi tập hoàn chỉnh của người dùng,
 *         bao gồm thời gian bắt đầu/kết thúc, tổng calo tiêu thụ và ghi chú.
 *         Liên kết với kế hoạch tập (WorkoutPlan) nếu có, và
 *         chứa danh sách chi tiết từng bài (WorkoutLog).
 * Bảng DB: workout_sessions
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@ToString(exclude = "logs")
@EqualsAndHashCode(callSuper = true, exclude = "logs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE workout_sessions SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class WorkoutSession extends BaseEntity {

    // Người thực hiện buổi tập
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Kế hoạch tập (không bắt buộc - người dùng có thể tập tự do)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id")
    private WorkoutPlan workoutPlan;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalCaloriesBurned;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Danh sách chi tiết từng bài tập trong buổi
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutLog> logs = new ArrayList<>();
}
