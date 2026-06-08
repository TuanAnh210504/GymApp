/**
 * THỰC THỂ: Ảnh tiến độ (ProgressPhoto)
 * Mô tả: Lưu ảnh theo dõi sự thay đổi hình thể của người dùng theo thời gian,
 *         kèm theo cân nặng và tỉ lệ mỡ cơ thể tại thời điểm chụp.
 *         Dùng để trực quan hoá hành trình tập luyện.
 * Bảng DB: progress_photos
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "progress_photos")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE progress_photos SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class ProgressPhoto extends BaseEntity {

    // Người dùng sở hữu ảnh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String imageUrl; // Đường dẫn ảnh (lưu trên server hoặc cloud)

    private Double weightAtTime; // Cân nặng tại thời điểm chụp (kg)

    private Double bodyFatPercentage; // Tỉ lệ mỡ cơ thể (%) - tuỳ chọn

    @Column(nullable = false)
    private LocalDate capturedAt; // Ngày chụp

    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú ngắn
}
