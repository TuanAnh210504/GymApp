package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;
import com.aigym.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyInfo(@Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateMyInfo(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin tài khoản thành công", response));
    }
}
