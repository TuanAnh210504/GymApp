package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.ProgressPhoto.ProgressPhotoRequest;
import com.aigym.dto.ProgressPhoto.ProgressPhotoResponse;
import com.aigym.service.ProgressPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress-photos")
@RequiredArgsConstructor
public class ProgressPhotoController {

    private final ProgressPhotoService progressPhotoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProgressPhotoResponse>> createProgressPhoto(
            @Valid @RequestBody ProgressPhotoRequest request) {
        ProgressPhotoResponse response = progressPhotoService.createProgressPhoto(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm ảnh tiến độ thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgressPhotoResponse>> getProgressPhotoById(@PathVariable Long id) {
        ProgressPhotoResponse response = progressPhotoService.getProgressPhotoById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgressPhotoResponse>>> getMyProgressPhotos() {
        List<ProgressPhotoResponse> responses = progressPhotoService.getMyProgressPhotos();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgressPhotoResponse>> updateProgressPhoto(
            @PathVariable Long id,
            @Valid @RequestBody ProgressPhotoRequest request) {
        ProgressPhotoResponse response = progressPhotoService.updateProgressPhoto(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh tiến độ thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgressPhoto(@PathVariable Long id) {
        progressPhotoService.deleteProgressPhoto(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá ảnh tiến độ thành công", null));
    }
}
