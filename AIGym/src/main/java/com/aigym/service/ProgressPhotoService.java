package com.aigym.service;

import com.aigym.dto.ProgressPhoto.ProgressPhotoRequest;
import com.aigym.dto.ProgressPhoto.ProgressPhotoResponse;

import java.util.List;

public interface ProgressPhotoService {

    ProgressPhotoResponse createProgressPhoto(ProgressPhotoRequest request);

    ProgressPhotoResponse getProgressPhotoById(Long id);

    List<ProgressPhotoResponse> getMyProgressPhotos();

    ProgressPhotoResponse updateProgressPhoto(Long id, ProgressPhotoRequest request);

    void deleteProgressPhoto(Long id);
}
