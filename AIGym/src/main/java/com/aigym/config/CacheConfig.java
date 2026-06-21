package com.aigym.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cấu hình Spring Cache với Caffeine (in-memory).
 * Dùng để cache các dữ liệu công khai (public/master data) ít thay đổi:
 *   - "public-workout-plans": Danh sách giáo án mẫu công khai
 *
 * TTL = 10 phút: Sau 10 phút không được gọi, cache tự expire.
 * maxSize = 500: Tối đa 500 entry, tránh tràn bộ nhớ.
 *
 * Khi Admin tạo/sửa/xóa WorkoutPlan -> WorkoutPlanServiceImpl dùng @CacheEvict
 * để xóa cache cũ, đảm bảo dữ liệu luôn nhất quán.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(List.of("public-workout-plans"));
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500));
        return manager;
    }
}
