package com.aigym.security;

import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.User;
import com.aigym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Dịch vụ dùng chung để lấy thông tin User đang đăng nhập.
 * Giải quyết vi phạm DRY: thay vì copy-paste getCurrentUser() ở 6 Service khác nhau,
 * tất cả đều inject class này.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    /**
     * Lấy User entity từ SecurityContext.
     * @return User đang đăng nhập
     * @throws NotFoundException nếu không tìm thấy user trong DB
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user đang đăng nhập"));
    }
}
