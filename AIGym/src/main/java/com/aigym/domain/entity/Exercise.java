/**
 * THỰC THỂ: Bài tập (Exercise)
 * Mô tả: Thư viện các bài tập trong hệ thống, bao gồm tên, mô tả hướng dẫn,
 *         nhóm cơ (Category), mức độ khó (Difficulty), dụng cụ cần thiết
 *         và link ảnh/video minh hoạ. Chỉ Admin mới có quyền thêm/sửa/xoá.
 * Bảng DB: exercises
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.Category;
import com.aigym.domain.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "exercises")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE exercises SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class Exercise extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name; // Tên bài tập

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // Hướng dẫn tập

    // Nhóm cơ mục tiêu chính
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category primaryCategory;

    // Danh sách các nhóm cơ tác động phụ
    @Builder.Default
    @ElementCollection(targetClass = Category.class)
    @CollectionTable(name = "exercise_secondary_categories", joinColumns = @JoinColumn(name = "exercise_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category_name")
    private Set<Category> secondaryCategories = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty; // Mức độ khó

    private String equipment; // Dụng cụ (null = bằng thể trọng)

    private String imageUrl; // Ảnh minh hoạ

    private String videoUrl; // Link video hướng dẫn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUserID;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = false;
}
