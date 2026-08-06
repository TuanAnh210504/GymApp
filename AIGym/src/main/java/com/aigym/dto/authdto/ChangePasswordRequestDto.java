package com.aigym.dto.authdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$",
        message = "Mật khẩu mới phải từ 6 ký tự, gồm ít nhất 1 chữ cái và 1 số"
    )
    private String newPassword;
}
