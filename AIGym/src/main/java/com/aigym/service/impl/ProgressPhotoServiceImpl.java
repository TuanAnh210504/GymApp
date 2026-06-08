package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.ProgressPhoto;
import com.aigym.domain.entity.User;
import com.aigym.dto.ProgressPhoto.ProgressPhotoRequest;
import com.aigym.dto.ProgressPhoto.ProgressPhotoResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ProgressPhotoRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.ProgressPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressPhotoServiceImpl implements ProgressPhotoService {

    private final ProgressPhotoRepository progressPhotoRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public ProgressPhotoResponse createProgressPhoto(ProgressPhotoRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        ProgressPhoto photo = genericMapper.mapToEntity(request, ProgressPhoto.class);
        photo.setUser(currentUser);

        ProgressPhoto saved = progressPhotoRepository.save(photo);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressPhotoResponse getProgressPhotoById(Long id) {
        ProgressPhoto photo = getPhotoAndVerifyOwnership(id);
        return toResponse(photo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressPhotoResponse> getMyProgressPhotos() {
        User currentUser = currentUserService.getCurrentUser();
        List<ProgressPhoto> photos = progressPhotoRepository.findByUserIdOrderByCapturedAtDesc(currentUser.getId());
        return photos.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ProgressPhotoResponse updateProgressPhoto(Long id, ProgressPhotoRequest request) {
        ProgressPhoto photo = getPhotoAndVerifyOwnership(id);

        photo.setImageUrl(request.getImageUrl());
        photo.setWeightAtTime(request.getWeightAtTime());
        photo.setBodyFatPercentage(request.getBodyFatPercentage());
        photo.setCapturedAt(request.getCapturedAt());
        photo.setNotes(request.getNotes());

        ProgressPhoto updated = progressPhotoRepository.save(photo);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProgressPhoto(Long id) {
        ProgressPhoto photo = getPhotoAndVerifyOwnership(id);
        progressPhotoRepository.delete(photo);
    }

    private ProgressPhoto getPhotoAndVerifyOwnership(Long id) {
        ProgressPhoto photo = progressPhotoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ảnh với ID: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!photo.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập ảnh này");
        }
        return photo;
    }

    private ProgressPhotoResponse toResponse(ProgressPhoto photo) {
        return genericMapper.mapToDto(photo, ProgressPhotoResponse.class);
    }
}
