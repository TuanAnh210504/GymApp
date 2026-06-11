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
import com.aigym.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation của ExerciseService.
 * Tuân thủ SRP: Class này chỉ chịu trách nhiệm xử lý nghiệp vụ Exercise.
 * Tuân thủ OCP: Có thể mở rộng bằng cách thêm method mới mà không cần sửa code cũ.
 */
@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public ExerciseResponse createExercise(ExerciseRequest request) {
        // Kiểm tra tên bài tập đã tồn tại chưa
        if (exerciseRepository.existsByName(request.getName())) {
            throw new BadRequestException("Bài tập với tên '" + request.getName() + "' đã tồn tại");
        }

        // Lấy user hiện tại đang đăng nhập
        User currentUser = currentUserService.getCurrentUser();

        // Sử dụng GenericMapper để map từ DTO sang Entity
        Exercise exercise = genericMapper.mapToEntity(request, Exercise.class);
        exercise.setCreatedByUserID(currentUser);
        // isPublic đã được map từ request

        Exercise saved = exerciseRepository.save(exercise);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài tập với ID: " + id));
        return toResponse(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        List<Exercise> exercises = exerciseRepository.findAll();
        // Sử dụng mapListToDto của GenericMapper và chỉnh sửa lại trường User
        return exercises.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByPrimaryCategory(Category category) {
        List<Exercise> exercises = exerciseRepository.findByPrimaryCategory(category);
        return exercises.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ExerciseResponse updateExercise(Long id, ExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài tập với ID: " + id));

        // Kiểm tra tên trùng (nếu đổi tên)
        if (!exercise.getName().equals(request.getName())
                && exerciseRepository.existsByName(request.getName())) {
            throw new BadRequestException("Bài tập với tên '" + request.getName() + "' đã tồn tại");
        }

        // Có thể map thủ công hoặc dùng mapper. mapToEntity sẽ tạo ra đối tượng mới,
        // nên đối với update, ta thường copy properties. ModelMapper cung cấp map(source, destination).
        // Tuy nhiên GenericMapper hiện tại chỉ có mapToEntity tạo mới.
        // Để giữ tính toàn vẹn Hibernate, ta cập nhật các trường thủ công hoặc thêm method vào GenericMapper.
        // Dùng thủ công cho Update để an toàn cho Hibernate Entity:
        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setPrimaryCategory(request.getPrimaryCategory());
        exercise.setSecondaryCategories(request.getSecondaryCategories());
        exercise.setDifficulty(request.getDifficulty());
        exercise.setEquipment(request.getEquipment());
        exercise.setImageUrl(request.getImageUrl());
        exercise.setVideoUrl(request.getVideoUrl());
        exercise.setPublic(request.isPublic());

        Exercise updated = exerciseRepository.save(exercise);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteExercise(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài tập với ID: " + id));
        exerciseRepository.delete(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getDeletedExercises() {
        List<Exercise> exercises = exerciseRepository.findAllDeletedNative();
        return exercises.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void restoreExercise(Long id) {
        int updated = exerciseRepository.restoreNative(id);
        if (updated == 0) {
            throw new NotFoundException("Không tìm thấy bài tập đã xóa với ID: " + id);
        }
    }

    // ======================== PRIVATE HELPER METHODS ========================

    /**
     * Chuyển đổi Entity → Response DTO sử dụng GenericMapper
     */
    private ExerciseResponse toResponse(Exercise exercise) {
        // Map các trường cơ bản
        ExerciseResponse response = genericMapper.mapToDto(exercise, ExerciseResponse.class);
        
        // Custom map cho trường User vì tên field khác nhau (createdByUserID vs createdByUser)
        if (exercise.getCreatedByUserID() != null) {
            response.setCreatedByUser(genericMapper.mapToDto(exercise.getCreatedByUserID(), UserResponse.class));
        }
        return response;
    }
}
