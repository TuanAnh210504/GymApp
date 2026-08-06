package com.aigym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Cấu hình bật JPA Auditing cho Spring Data.
 * Giúp tự động điền giá trị cho @CreatedBy, @CreatedDate
 * trong BaseEntity mỗi khi tạo entity mới.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
