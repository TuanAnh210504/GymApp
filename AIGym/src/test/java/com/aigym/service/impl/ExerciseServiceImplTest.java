package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.Exercise;
import com.aigym.domain.entity.User;
import com.aigym.domain.enums.Category;
import com.aigym.dto.Exercise.ExerciseRequest;
import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.ExerciseRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceImplTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private ExerciseServiceImpl exerciseService;

    private User currentUser;
    private Exercise exercise;
    private ExerciseRequest exerciseRequest;
    private ExerciseResponse exerciseResponse;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .email("test@yo.com")
                .build();
        currentUser.setId(1L);

        exercise = Exercise.builder()
                .name("Push Up")
                .primaryCategory(Category.CHEST_MIDDLE)
                .build();
        exercise.setId(1L);
        exercise.setCreatedByUserID(currentUser);

        exerciseRequest = new ExerciseRequest();
        exerciseRequest.setName("Push Up");
        exerciseRequest.setPrimaryCategory(Category.CHEST_MIDDLE);

        exerciseResponse = new ExerciseResponse();
        exerciseResponse.setId(1L);
        exerciseResponse.setName("Push Up");
    }

    @Test
    void createExercise_Success() {
        when(exerciseRepository.existsByName("Push Up")).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(genericMapper.mapToEntity(exerciseRequest, Exercise.class)).thenReturn(exercise);
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exercise);
        when(genericMapper.mapToDto(exercise, ExerciseResponse.class)).thenReturn(exerciseResponse);
        when(genericMapper.mapToDto(currentUser, UserResponse.class)).thenReturn(new UserResponse());

        ExerciseResponse response = exerciseService.createExercise(exerciseRequest);

        assertNotNull(response);
        assertEquals("Push Up", response.getName());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
    }

    @Test
    void createExercise_Fail_NameExists() {
        when(exerciseRepository.existsByName("Push Up")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> exerciseService.createExercise(exerciseRequest));
        verify(exerciseRepository, never()).save(any(Exercise.class));
    }

    @Test
    void getExerciseById_Success() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(genericMapper.mapToDto(exercise, ExerciseResponse.class)).thenReturn(exerciseResponse);
        
        ExerciseResponse response = exerciseService.getExerciseById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getExerciseById_Fail_NotFound() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> exerciseService.getExerciseById(1L));
    }

    @Test
    void updateExercise_Success() {
        ExerciseRequest updateRequest = new ExerciseRequest();
        updateRequest.setName("Pull Up");

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.existsByName("Pull Up")).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exercise);
        
        ExerciseResponse updatedResponse = new ExerciseResponse();
        updatedResponse.setName("Pull Up");
        when(genericMapper.mapToDto(exercise, ExerciseResponse.class)).thenReturn(updatedResponse);

        ExerciseResponse response = exerciseService.updateExercise(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Pull Up", response.getName());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
    }

    @Test
    void bulkDeleteExercises_Success() {
        List<Long> ids = List.of(1L, 2L);
        when(exerciseRepository.findAllById(ids)).thenReturn(List.of(exercise, new Exercise()));

        assertDoesNotThrow(() -> exerciseService.bulkDeleteExercises(ids));

        verify(exerciseRepository, times(1)).deleteAll(any());
    }

    @Test
    void restoreExercise_Success() {
        when(exerciseRepository.restoreNative(1L)).thenReturn(1);

        assertDoesNotThrow(() -> exerciseService.restoreExercise(1L));
        verify(exerciseRepository, times(1)).restoreNative(1L);
    }
}
