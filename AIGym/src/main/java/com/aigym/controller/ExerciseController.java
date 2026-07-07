package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.domain.enums.Category;
import com.aigym.dto.Exercise.ExerciseRequest;
import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Exercise CRUD.
 * Tuân thủ SRP: Controller chỉ nhận request, gọi Service, trả response.
 */
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExerciseResponse>> createExercise(
            @Valid @RequestBody ExerciseRequest request) {
        ExerciseResponse response = exerciseService.createExercise(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài tập thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> getExerciseById(@PathVariable Long id) {
        ExerciseResponse response = exerciseService.getExerciseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ── FIX: Thêm Pageable để tránh OOM khi dữ liệu lớn.
     * Mặc định: page=0, size=20, sort=name,asc
     * Ví dụ: GET /api/exercises?page=0&size=20&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExerciseResponse>>> getAllExercises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortDir, sort));
        Page<ExerciseResponse> responses = exerciseService.getAllExercises(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * ── FIX: Thêm Pageable cho lọc theo category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<ExerciseResponse>>> getByPrimaryCategory(
            @PathVariable Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name"));
        Page<ExerciseResponse> responses = exerciseService.getExercisesByPrimaryCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ExerciseResponse>>> searchExercises(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name"));
        Page<ExerciseResponse> responses = exerciseService.searchExercises(q, category, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRequest request) {
        ExerciseResponse response = exerciseService.updateExercise(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài tập thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá bài tập thành công", null));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteExercises(@RequestBody List<Long> ids) {
        exerciseService.bulkDeleteExercises(ids);
        return ResponseEntity.ok(ApiResponse.success("Xoá " + ids.size() + " bài tập thành công", null));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getDeletedExercises() {
        List<ExerciseResponse> responses = exerciseService.getDeletedExercises();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreExercise(@PathVariable Long id) {
        exerciseService.restoreExercise(id);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục bài tập thành công", null));
    }
}
