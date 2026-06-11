package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "workout_plan_exercises")
@Getter
@Setter
@ToString(exclude = {"workoutPlanDay", "exercise"})
@EqualsAndHashCode(callSuper = true, exclude = {"workoutPlanDay", "exercise"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE workout_plan_exercises SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class WorkoutPlanExercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_day_id", nullable = false)
    private WorkoutPlanDay workoutPlanDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private Integer targetSets;
    private Integer targetReps;
    private Double targetWeight; // Nullable
    
    @Column(nullable = false)
    private Integer orderIndex;

    @Column(columnDefinition = "TEXT")
    private String note;
}
