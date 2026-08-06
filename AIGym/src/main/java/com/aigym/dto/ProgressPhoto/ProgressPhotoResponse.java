package com.aigym.dto.ProgressPhoto;

import com.aigym.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressPhotoResponse {
    private Long id;
    private UserResponse user;
    private String imageUrl;
    private Double weightAtTime;
    private Double bodyFatPercentage;
    private LocalDate capturedAt;
    private String notes;
}
