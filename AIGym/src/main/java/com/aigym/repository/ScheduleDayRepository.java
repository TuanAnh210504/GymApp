package com.aigym.repository;

import com.aigym.domain.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleDayRepository extends JpaRepository<ScheduleDay, Long> {
}
