package com.aigym.service;

import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;
import com.aigym.dto.user.UserAdminResponse;

import java.util.List;

public interface UserService {
    UserResponse updateMyInfo(UpdateUserRequest request);
    List<UserAdminResponse> getAllUsers();
    UserAdminResponse toggleUserStatus(Long id, boolean enabled);
    void changeUserRole(Long id, String role);
    void deleteUser(Long id);
}
