package com.aigym.dto.NutritionLog;

import com.aigym.domain.enums.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionLogRequest {

    @NotNull(message = "ID thực phẩm không được để trống")
    private Long foodItemId;

    @NotNull(message = "Khối lượng không được để trống")
    @Min(value = 1, message = "Khối lượng phải lớn hơn 0")
    private Double amount; // số gram

    @NotNull(message = "Loại bữa ăn không được để trống")
    private MealType mealType;

    @NotNull(message = "Ngày ghi không được để trống")
    private LocalDate loggedAt;
}
