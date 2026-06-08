package com.aigym.repository;

import com.aigym.domain.entity.WeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {
    List<WeeklySchedule> findByUserId(Long userId);
    Optional<WeeklySchedule> findByUserIdAndIsActiveTrue(Long userId);
}
