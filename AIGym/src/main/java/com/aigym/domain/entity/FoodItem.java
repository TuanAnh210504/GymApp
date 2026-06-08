/**
 * THỰC THỂ: Thực phẩm (FoodItem)
 * Mô tả: Thư viện dữ liệu dinh dưỡng của các loại thực phẩm,
 *         lưu giá trị calo, đạm, tinh bột, chất béo và chất xơ
 *         tính trên mỗi 100g. Dùng làm tham chiếu cho NutritionLog.
 * Bảng DB: food_items
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "food_items")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE food_items SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class FoodItem extends BaseEntity {

    @Column(nullable = false)
    private String name; // Tên thực phẩm

    private String brand; // Thương hiệu

    @Column(nullable = false)
    private Integer caloriesPer100g; // Calo mỗi 100g

    @Column(nullable = false)
    private Double protein; // Đạm (g/100g)

    @Column(nullable = false)
    private Double carbs; // Tinh bột (g/100g)

    @Column(nullable = false)
    private Double fat; // Chất béo (g/100g)

    private Double fiber; // Chất xơ (g/100g) - tuỳ chọn
}
