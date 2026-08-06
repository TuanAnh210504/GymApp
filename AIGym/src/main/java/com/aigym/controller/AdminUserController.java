package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.user.UserAdminResponse;
import com.aigym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", userService.getAllUsers()));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserAdminResponse>> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", true);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", userService.toggleUserStatus(id, enabled)));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        userService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Thay đổi vai trò thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }
}
