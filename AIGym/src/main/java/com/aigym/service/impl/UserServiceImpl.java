package com.aigym.service.impl;

import com.aigym.domain.entity.User;
import com.aigym.domain.entity.UserProfile;
import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;
import com.aigym.dto.user.UserAdminResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.UserRepository;
import com.aigym.repository.UserProfileRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public UserResponse updateMyInfo(UpdateUserRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        
        currentUser.setFullName(request.getFullName());
        
        User savedUser = userRepository.save(currentUser);
        return genericMapper.mapToDto(savedUser, UserResponse.class);
    }

    @Override
    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserAdminResponse toggleUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setActive(enabled);
        return mapToAdminResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changeUserRole(Long id, String role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        try {
            user.setRole(com.aigym.domain.enums.Role.valueOf(role));
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ");
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserAdminResponse mapToAdminResponse(User user) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isActive())
                .createdAt(user.getCreatedAt())
                .avatarUrl(profileOpt.map(UserProfile::getAvatarUrl).orElse(null))
                .build();
    }
}
