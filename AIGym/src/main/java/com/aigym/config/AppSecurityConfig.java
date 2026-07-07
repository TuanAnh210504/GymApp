package com.aigym.config;

import com.aigym.domain.entity.User;
import com.aigym.domain.enums.Role;
import com.aigym.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AppSecurityConfig.class);

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

    // 4. Khởi tạo tài khoản Admin từ biến môi trường
    // ── FIX: Chỉ chạy khi biến môi trường APP_INIT_DATA=true
    // Password lấy từ ADMIN_PASSWORD, không hardcode.
    // KHÔNG tạo user thường với mật khẩu mặc định dễ đoán.
    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String initData = System.getenv("APP_INIT_DATA");
            if (!"true".equalsIgnoreCase(initData)) {
                log.info("Skipping default data initialization. Set APP_INIT_DATA=true to enable.");
                return;
            }

            String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@aigym.com");
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("ADMIN_PASSWORD env var is not set. Skipping admin account creation.");
                return;
            }

            User admin = userRepository.findByEmail(adminEmail).orElse(new User());
            admin.setEmail(adminEmail);
            admin.setFullName("Admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Admin account initialized for email: {}", adminEmail);
        };
    }
}
