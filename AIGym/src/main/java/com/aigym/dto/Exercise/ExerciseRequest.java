package com.aigym.dto.Exercise;

import com.aigym.domain.enums.Category;
import com.aigym.domain.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO dùng cho request tạo mới Exercise.
 * Tách riêng khỏi Update để tuân thủ SRP (Single Responsibility Principle).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseRequest {

    @NotBlank(message = "Tên bài tập không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Nhóm cơ chính không được để trống")
    private Category primaryCategory;

    private Set<Category> secondaryCategories = new HashSet<>();

    @NotNull(message = "Mức độ khó không được để trống")
    private Difficulty difficulty;

    private String equipment;

    private String imageUrl;

    private List<String> instructionImageUrls;

    private String videoUrl;

    private Boolean isPublic = false;
}
