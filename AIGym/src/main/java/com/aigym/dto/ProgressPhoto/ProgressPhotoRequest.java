package com.aigym.dto.ProgressPhoto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressPhotoRequest {

    @NotBlank(message = "Đường dẫn ảnh không được để trống")
    private String imageUrl;

    private Double weightAtTime;

    private Double bodyFatPercentage;

    @NotNull(message = "Ngày chụp không được để trống")
    private LocalDate capturedAt;

    private String notes;
}
