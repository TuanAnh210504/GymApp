package com.aigym.repository;

import com.aigym.domain.enums.Category;
import com.aigym.domain.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByPrimaryCategory(Category category);

    boolean existsByName(String name);
}
