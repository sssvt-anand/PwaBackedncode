package com.room.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.room.app.config.JwtUtil;
import com.room.app.dto.ForgotPasswordRequest;
import com.room.app.dto.ResetPasswordRequest;
import com.room.app.dto.UserResponse;
import com.room.app.entity.User;
import com.room.app.exception.OTPException;
import com.room.app.exception.ResourceNotFoundException;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final OTPService otpService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserDetailsService userDetailsService, 
                     PasswordEncoder passwordEncoder,
                     JwtUtil jwtUtil,
                     UserService userService,
                     OTPService otpService,
                     AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.otpService = otpService;
        this.authenticationManager = authenticationManager;
    }

    public Map<String, Object> login(User user) throws AuthenticationException {
        Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User customUser = userService.findByEmail(user.getEmail());
        
        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_USER")
            .replace("ROLE_", "");
        
        String token = jwtUtil.generateToken(userDetails);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", userDetails.getUsername());
        response.put("name", customUser.getName());
        response.put("role", role);
        
        return response;
    }

    public Map<String, String> register(User user) throws Exception {
        Map<String, String> response = new HashMap<>();
        userService.register(user);
        response.put("status", "success");
        response.put("message", "User registered successfully");
        return response;
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) throws OTPException {
        if (!userService.existsByEmail(request.getEmail())) {
            throw new OTPException("Email not registered");
        }
        
        String otp = otpService.generateOTP(request.getEmail());
        otpService.sendOTPEmail(request.getEmail(), otp);
        
        return Map.of(
            "status", "success",
            "message", "OTP sent to your email"
        );
    }

    public Map<String, String> resetPassword(ResetPasswordRequest request) throws OTPException {
        if (!otpService.validateOTP(request.getEmail(), request.getOtp())) {
            throw new OTPException("Invalid or expired OTP");
        }
        
        userService.updatePassword(request.getEmail(), request.getNewPassword());
        otpService.clearOTP(request.getEmail());
        
        return Map.of(
            "status", "success",
            "message", "Password updated successfully"
        );
    }

    public Map<String, Object> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Unauthorized");
        }

        String email = authentication.getName();
        User user = (User) userDetailsService.loadUserByUsername(email);

        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole().replace("ROLE_", ""));

        return response;
    }

    public Map<String, String> logout() {
        SecurityContextHolder.clearContext();
        return Map.of(
            "status", "success",
            "message", "Logged out successfully"
        );
    }

    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    public UserResponse updateUserRole(Long userId, String newRole) throws ResourceNotFoundException {
        return userService.updateUserRole(userId, newRole);
    }
}