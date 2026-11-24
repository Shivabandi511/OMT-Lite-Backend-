package com.OMT.Backend.controller;

import com.OMT.Backend.dto.*;
import com.OMT.Backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-mfa-init")
    public ResponseEntity<Map<String, Object>> loginMfaInit(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = authService.initiateLoginMfa(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-mfa-verify")
    public ResponseEntity<AuthResponse> loginMfaVerify(@Valid @RequestBody OtpVerifyRequest request) {
        AuthResponse response = authService.verifyMfaAndLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody OtpRequest request) {
        Map<String, Object> response = authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        Map<String, Object> response = authService.verifyOtpOnly(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        Map<String, Object> response = authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody Map<String, String> request) {
        UserDTO user = authService.updateProfile(request.get("name"));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = authService.changePassword(
                request.get("currentPassword"),
                request.get("newPassword")
        );
        return ResponseEntity.ok(response);
    }
}
