package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.ProgressPhoto;
import com.aigym.domain.entity.User;
import com.aigym.dto.ProgressPhoto.ProgressPhotoRequest;
import com.aigym.dto.ProgressPhoto.ProgressPhotoResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ProgressPhotoRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressPhotoServiceImplTest {

    @Mock
    private ProgressPhotoRepository progressPhotoRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private ProgressPhotoServiceImpl progressPhotoService;

    private User currentUser;
    private ProgressPhoto photo;
    private ProgressPhotoRequest request;
    private ProgressPhotoResponse response;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("test@yo.com").build();
        currentUser.setId(1L);

        photo = new ProgressPhoto();
        photo.setId(1L);
        photo.setUser(currentUser);
        photo.setImageUrl("http://image.com/1.jpg");
        photo.setWeightAtTime(70.0);

        request = new ProgressPhotoRequest();
        request.setImageUrl("http://image.com/1.jpg");
        request.setWeightAtTime(70.0);
        request.setCapturedAt(LocalDate.now());

        response = new ProgressPhotoResponse();
        response.setId(1L);
        response.setImageUrl("http://image.com/1.jpg");
    }

    @Test
    void createProgressPhoto_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToEntity(request, ProgressPhoto.class)).thenReturn(photo);
        when(progressPhotoRepository.save(any(ProgressPhoto.class))).thenReturn(photo);
        when(genericMapper.mapToDto(photo, ProgressPhotoResponse.class)).thenReturn(response);

        ProgressPhotoResponse res = progressPhotoService.createProgressPhoto(request);

        assertNotNull(res);
        assertEquals("http://image.com/1.jpg", res.getImageUrl());
        verify(progressPhotoRepository, times(1)).save(photo);
    }

    @Test
    void getProgressPhotoById_Success() {
        when(progressPhotoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToDto(photo, ProgressPhotoResponse.class)).thenReturn(response);

        ProgressPhotoResponse res = progressPhotoService.getProgressPhotoById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void getProgressPhotoById_Fail_NotOwner() {
        User otherUser = User.builder().build();
        otherUser.setId(2L);
        when(progressPhotoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(BadRequestException.class, () -> progressPhotoService.getProgressPhotoById(1L));
    }

    @Test
    void getMyProgressPhotos_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(progressPhotoRepository.findByUserIdOrderByCapturedAtDesc(1L)).thenReturn(List.of(photo));
        when(genericMapper.mapToDto(photo, ProgressPhotoResponse.class)).thenReturn(response);

        List<ProgressPhotoResponse> res = progressPhotoService.getMyProgressPhotos();

        assertFalse(res.isEmpty());
        assertEquals(1, res.size());
    }

    @Test
    void updateProgressPhoto_Success() {
        request.setWeightAtTime(75.0);
        when(progressPhotoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(progressPhotoRepository.save(any(ProgressPhoto.class))).thenReturn(photo);
        when(genericMapper.mapToDto(photo, ProgressPhotoResponse.class)).thenReturn(response);

        ProgressPhotoResponse res = progressPhotoService.updateProgressPhoto(1L, request);

        assertNotNull(res);
        verify(progressPhotoRepository, times(1)).save(photo);
    }

    @Test
    void deleteProgressPhoto_Success() {
        when(progressPhotoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertDoesNotThrow(() -> progressPhotoService.deleteProgressPhoto(1L));
        verify(progressPhotoRepository, times(1)).delete(photo);
    }
}
