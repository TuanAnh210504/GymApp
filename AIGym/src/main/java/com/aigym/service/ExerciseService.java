package com.aigym.service;

import com.aigym.domain.enums.Category;
import com.aigym.dto.Exercise.ExerciseRequest;
import com.aigym.dto.Exercise.ExerciseResponse;

import java.util.List;

/**
 * Interface Service cho Exercise.
 * Tuân thủ ISP (Interface Segregation Principle): chỉ khai báo các hành vi
 * liên quan đến nghiệp vụ Exercise.
 * Tuân thủ DIP (Dependency Inversion Principle): Controller phụ thuộc vào
 * interface này, không phụ thuộc vào implementation cụ thể.
 */
public interface ExerciseService {

    ExerciseResponse createExercise(ExerciseRequest request);

    ExerciseResponse getExerciseById(Long id);

    List<ExerciseResponse> getAllExercises();

    List<ExerciseResponse> getExercisesByPrimaryCategory(Category category);

    ExerciseResponse updateExercise(Long id, ExerciseRequest request);

    void deleteExercise(Long id);
}
