package com.aigym.service.impl;

import com.aigym.domain.entity.User;
import com.aigym.dto.user.UpdateUserRequest;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.UserRepository;
import com.aigym.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GenericMapper genericMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .email("test@gmail.com")
                .fullName("Old Name")
                .build();
        currentUser.setId(1L);
    }

    @Test
    void updateMyInfo_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("New Name");

        UserResponse mockResponse = new UserResponse();
        mockResponse.setFullName("New Name");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenReturn(currentUser);
        when(genericMapper.mapToDto(any(User.class), eq(UserResponse.class))).thenReturn(mockResponse);

        UserResponse response = userService.updateMyInfo(request);

        assertNotNull(response);
        assertEquals("New Name", response.getFullName());
        assertEquals("New Name", currentUser.getFullName());

        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    void updateMyInfo_Fail_CurrentUserException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("New Name");

        when(currentUserService.getCurrentUser()).thenThrow(new RuntimeException("Not authenticated"));

        assertThrows(RuntimeException.class, () -> userService.updateMyInfo(request));

        verify(userRepository, never()).save(any());
        verify(genericMapper, never()).mapToDto(any(), any());
    }
}
