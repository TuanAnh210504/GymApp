package com.aigym.repository;

import com.aigym.domain.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    Page<FoodItem> findAll(Pageable pageable);

    List<FoodItem> findByName(String name);
    boolean existsByName(String name);
    
    Optional<FoodItem> findByNameIgnoreCaseAndIsPublicTrue(String name);
    Optional<FoodItem> findByNameIgnoreCaseAndIsPublicFalseAndUserId(String name, Long userId);
}
