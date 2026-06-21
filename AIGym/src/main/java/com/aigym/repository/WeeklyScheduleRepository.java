package com.aigym.repository;

import com.aigym.domain.entity.WeeklySchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

    // --- Dùng cho READ: fetch toàn bộ cây dữ liệu bằng JOIN (tránh N+1 query) ---
    @EntityGraph(attributePaths = {
            "scheduleDays",
            "scheduleDays.scheduledExercises",
            "scheduleDays.scheduledExercises.exercise"
    })
    List<WeeklySchedule> findWithDetailsByUserId(Long userId);

    @EntityGraph(attributePaths = {
            "scheduleDays",
            "scheduleDays.scheduledExercises",
            "scheduleDays.scheduledExercises.exercise"
    })
    Optional<WeeklySchedule> findWithDetailsById(Long id);

    // --- Dùng cho các tác vụ nhẹ (deactivate, check active) ---
    List<WeeklySchedule> findByUserId(Long userId);
    Optional<WeeklySchedule> findByUserIdAndIsActiveTrue(Long userId);
}
