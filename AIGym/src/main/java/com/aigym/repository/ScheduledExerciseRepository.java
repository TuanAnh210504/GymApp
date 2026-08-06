package com.aigym.repository;

import com.aigym.domain.entity.ScheduledExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledExerciseRepository extends JpaRepository<ScheduledExercise, Long> {
}
