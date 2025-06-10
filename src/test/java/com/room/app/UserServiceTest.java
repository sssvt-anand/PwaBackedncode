package com.room.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.room.app.dto.User;
import com.room.app.dto.UserResponse;
import com.room.app.exception.ResourceNotFoundException;
import com.room.app.repository.UserRepository;
import com.room.app.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole("ROLE_USER");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("adminpass");
        adminUser.setRole("ROLE_ADMIN");
    }

    // Register method tests
    @Test
    void register_ValidUser_Success() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.register(testUser);

        verify(userRepository, times(1)).existsByEmail(testUser.getEmail());
        verify(passwordEncoder, times(1)).encode(testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void register_EmptyName_ThrowsException() {
        testUser.setName(null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(testUser);
        });
        
        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void register_EmptyPassword_ThrowsException() {
        testUser.setPassword(null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(testUser);
        });
        
        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void register_EmptyEmail_ThrowsException() {
        testUser.setEmail(null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(testUser);
        });
        
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            userService.register(testUser);
        });
        
        assertEquals("Email already registered", exception.getMessage());
    }

    // getAllUsers method tests
    @Test
    void getAllUsers_ReturnsListOfUserResponses() {
        List<User> users = Arrays.asList(testUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Test User", result.get(0).getUsername());
        assertEquals("ADMIN", result.get(1).getRole()); // Testing ROLE_ prefix removal
    }

    @Test
    void getAllUsers_EmptyList_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    // updateUserRole method tests
    @Test
    void updateUserRole_ValidUserAndRole_ReturnsUpdatedUser() throws ResourceNotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUserRole(1L, "ADMIN");

        assertEquals("ADMIN", result.getRole());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserRole_WithRolePrefix_ReturnsUpdatedUser() throws ResourceNotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUserRole(1L, "ROLE_ADMIN");

        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void updateUserRole_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserRole(99L, "ADMIN");
        });
    }

    // findByEmail method tests
    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("Test User", result.getName());
    }

    @Test
    void findByEmail_NonExistingEmail_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findByEmail("nonexistent@example.com");
        });
    }

    // updatePassword method tests
    @Test
    void updatePassword_ValidUser_UpdatesPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updatePassword("test@example.com", "newPassword");

        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updatePassword_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updatePassword("nonexistent@example.com", "newPassword");
        });
    }

    // existsByEmail method tests
    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("nonexistent@example.com");

        assertFalse(result);
    }
}