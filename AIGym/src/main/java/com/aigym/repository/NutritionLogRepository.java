package com.aigym.repository;

import com.aigym.domain.entity.NutritionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {
    List<NutritionLog> findByUserIdAndLoggedAtOrderByMealTypeAsc(Long userId, LocalDate loggedAt);
    List<NutritionLog> findByUserId(Long userId);
}
