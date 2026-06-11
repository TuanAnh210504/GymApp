package com.aigym.service;

import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;

public interface UserService {
    UserResponse updateMyInfo(UpdateUserRequest request);
}
