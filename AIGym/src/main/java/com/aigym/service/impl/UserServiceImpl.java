package com.aigym.service.impl;

import com.aigym.domain.entity.User;
import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.UserRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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
}
