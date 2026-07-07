package com.aigym.repository;

import com.aigym.domain.enums.Category;
import com.aigym.domain.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Page<Exercise> findAll(Pageable pageable);

    Page<Exercise> findByPrimaryCategory(Category category, Pageable pageable);

    List<Exercise> findByPrimaryCategory(Category category);

    Page<Exercise> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Exercise> findByNameContainingIgnoreCaseAndPrimaryCategory(String name, Category category, Pageable pageable);

    boolean existsByName(String name);

    java.util.Optional<Exercise> findByNameIgnoreCase(String name);

    java.util.Optional<Exercise> findByNameIgnoreCaseAndIsPublicTrue(String name);

    java.util.Optional<Exercise> findByNameIgnoreCaseAndCreatedByUserID_Id(String name, Long userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM exercises WHERE is_deleted = true", nativeQuery = true)
    List<Exercise> findAllDeletedNative();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE exercises SET is_deleted = false WHERE id = :id", nativeQuery = true)
    int restoreNative(@org.springframework.data.repository.query.Param("id") Long id);
}
