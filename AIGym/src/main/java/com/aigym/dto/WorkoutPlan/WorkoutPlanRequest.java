package com.aigym.dto.WorkoutPlan;

import com.aigym.domain.enums.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanRequest {

    @NotBlank(message = "Tên giáo án không được để trống")
    private String title;

    @NotBlank(message = "Mô tả giáo án không được để trống")
    private String description;

    private String coverImageUrl;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng tối thiểu là 1 tuần")
    private Integer durationWeeks;

    @NotNull(message = "Mức độ khó không được để trống")
    private Difficulty difficulty;

    @com.fasterxml.jackson.annotation.JsonProperty("isPublic")
    private Boolean isPublic = false;

    private List<WorkoutPlanDayRequest> planDays;
}
