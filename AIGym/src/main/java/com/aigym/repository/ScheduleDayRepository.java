package com.aigym.repository;

import com.aigym.domain.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleDayRepository extends JpaRepository<ScheduleDay, Long> {

    // Hard-delete all exercises belonging to days of a schedule (bypass soft delete)
    @Modifying
    @Query(value = "DELETE FROM scheduled_exercises WHERE schedule_day_id IN " +
            "(SELECT id FROM schedule_days WHERE weekly_schedule_id = :scheduleId)",
            nativeQuery = true)
    void hardDeleteExercisesByScheduleId(@Param("scheduleId") Long scheduleId);

    // Hard-delete all schedule days of a schedule (bypass soft delete)
    @Modifying
    @Query(value = "DELETE FROM schedule_days WHERE weekly_schedule_id = :scheduleId",
            nativeQuery = true)
    void hardDeleteDaysByScheduleId(@Param("scheduleId") Long scheduleId);
}
