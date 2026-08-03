package com.aigym.dto.Exercise;

import com.aigym.domain.enums.Category;
import com.aigym.domain.enums.Difficulty;
import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO trả về cho client khi đọc dữ liệu Exercise.
 * Không bao giờ trả trực tiếp Entity ra ngoài (bảo mật + tuân thủ SRP).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseResponse {

    private Long id;

    private String name;

    private String description;

    private Category primaryCategory;

    @Builder.Default
    private Set<Category> secondaryCategories = new HashSet<>();

    private Difficulty difficulty;

    private String equipment;

    private String imageUrl;

    private List<String> instructionImageUrls;

    private String videoUrl;

    private UserResponse createdByUser;

    private Boolean isPublic;

    private LocalDateTime createdAt;
}
