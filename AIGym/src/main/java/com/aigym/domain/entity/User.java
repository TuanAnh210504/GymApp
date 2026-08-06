/**
 * THỰC THỂ: Người dùng (User)
 * Mô tả: Lưu thông tin tài khoản đăng nhập, phân quyền (USER/ADMIN) của hệ thống.
 * Bảng DB: users
 */
package com.aigym.domain.entity;

import com.aigym.domain.BaseEntity;
import com.aigym.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = false;

    private String verificationCode;

    private java.time.LocalDateTime verificationCodeExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private int failedOtpAttempts = 0;
}
