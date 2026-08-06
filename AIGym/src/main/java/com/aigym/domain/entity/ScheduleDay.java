/**
 * THỰC THỂ: Ngày tập trong lịch tuần (ScheduleDay)
 * Mô tả: Đại diện cho một thứ trong tuần (Thứ 2, Thứ 3...) bên trong một lịch tập.
 *         Người dùng bấm vào thứ tương ứng sẽ thấy danh sách bài tập đã xếp.
 *         Một ngày có thể để trống (ngày nghỉ) hoặc có nhiều bài tập.
 * Bảng DB: schedule_days
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "schedule_days",
    uniqueConstraints = @UniqueConstraint(columnNames = {"weekly_schedule_id", "day_of_week"})
)
@Getter
@Setter
@ToString(exclude = {"weeklySchedule", "scheduledExercises"})
@EqualsAndHashCode(callSuper = true, exclude = {"weeklySchedule", "scheduledExercises"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDay extends BaseEntity {

    // Thuộc lịch tập nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_schedule_id", nullable = false)
    private WeeklySchedule weeklySchedule;

    // Ngày trong tuần (Thứ 2 → Chủ nhật)
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRestDay = false; // Ngày nghỉ → không có bài tập

    // Danh sách bài tập đã xếp vào ngày này
    @OneToMany(mappedBy = "scheduleDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private Set<ScheduledExercise> scheduledExercises = new LinkedHashSet<>();
}
