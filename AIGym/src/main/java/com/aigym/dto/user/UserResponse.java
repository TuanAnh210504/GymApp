package com.aigym.dto.user;

import com.aigym.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    private String fullName;

    private Role role = Role.USER;
}
