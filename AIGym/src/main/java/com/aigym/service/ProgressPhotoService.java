package com.aigym.service;

import com.aigym.dto.ProgressPhoto.ProgressPhotoRequest;
import com.aigym.dto.ProgressPhoto.ProgressPhotoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProgressPhotoService {

    ProgressPhotoResponse createProgressPhoto(ProgressPhotoRequest request);

    ProgressPhotoResponse getProgressPhotoById(Long id);

    /** Trả về ảnh tiến độ của user hiện tại với phân trang (khuyến nghị). */
    Page<ProgressPhotoResponse> getMyProgressPhotos(Pageable pageable);

    /** Trả về toàn bộ list – chỉ dùng nội bộ. */
    List<ProgressPhotoResponse> getMyProgressPhotos();

    ProgressPhotoResponse updateProgressPhoto(Long id, ProgressPhotoRequest request);

    void deleteProgressPhoto(Long id);
}
