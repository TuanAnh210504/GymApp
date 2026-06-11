package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "workout_plan_days",
    uniqueConstraints = @UniqueConstraint(columnNames = {"workout_plan_id", "day_of_week"})
)
@Getter
@Setter
@ToString(exclude = {"workoutPlan", "planExercises"})
@EqualsAndHashCode(callSuper = true, exclude = {"workoutPlan", "planExercises"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE workout_plan_days SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class WorkoutPlanDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRestDay = false;

    @OneToMany(mappedBy = "workoutPlanDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<WorkoutPlanExercise> planExercises = new ArrayList<>();
}
