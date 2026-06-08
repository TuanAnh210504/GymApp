package com.aigym.config;

import com.aigym.domain.entity.User;
import com.aigym.domain.enums.Role;
import com.aigym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppSecurityConfig {

    // 1. Khởi tạo thuật toán mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // 2. Cấu hình Provider cho Spring Security
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 3. Cấu hình AuthenticationManager để dùng trong phần Login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. (Tùy chọn) Khởi tạo dữ liệu mẫu
    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Cập nhật hoặc tạo mới Admin
            User admin = userRepository.findByEmail("admin@aigym.com").orElse(new User());
            admin.setEmail("admin@aigym.com");
            admin.setFullName("Admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            // Cập nhật hoặc tạo mới User
            User user = userRepository.findByEmail("user@aigym.com").orElse(new User());
            user.setEmail("user@aigym.com");
            user.setFullName("User");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            user.setActive(true);
            userRepository.save(user);
        };
    }
}
