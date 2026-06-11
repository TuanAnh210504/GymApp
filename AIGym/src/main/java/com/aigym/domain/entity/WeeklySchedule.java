/**
 * THỰC THỂ: Lịch tập cá nhân theo tuần (WeeklySchedule)
 * Mô tả: Mỗi user có thể tạo nhiều lịch tập theo tuần (VD: "Lịch PPL", "Lịch Full Body").
 *         Chỉ một lịch được kích hoạt (isActive = true) tại một thời điểm.
 *         Lịch này chứa các ngày tập (ScheduleDay) được xếp vào các thứ trong tuần.
 * Bảng DB: weekly_schedules
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_schedules")
@Getter
@Setter
@ToString(exclude = "scheduleDays")
@EqualsAndHashCode(callSuper = true, exclude = "scheduleDays")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklySchedule extends BaseEntity {

    // Chủ sở hữu lịch tập
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name; // VD: "Lịch PPL tháng 5", "Lịch Full Body"

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả ngắn về lịch

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = false; // Chỉ 1 lịch active tại một thời điểm

    // Danh sách các ngày tập trong lịch
    @OneToMany(mappedBy = "weeklySchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleDay> scheduleDays = new ArrayList<>();
}
