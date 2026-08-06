package com.aigym.repository;

import com.aigym.domain.entity.ProgressPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressPhotoRepository extends JpaRepository<ProgressPhoto, Long> {
    Page<ProgressPhoto> findByUserId(Long userId, Pageable pageable);

    List<ProgressPhoto> findByUserIdOrderByCapturedAtDesc(Long userId);
}
