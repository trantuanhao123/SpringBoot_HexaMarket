package com.hexamarket.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.request.ForgotPasswordRequest;
import com.hexamarket.code.dto.request.LoginRequest;
import com.hexamarket.code.dto.request.ResetPasswordRequest;
import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.VerifyOtpRequest;
import com.hexamarket.code.dto.response.AuthResponse;
import com.hexamarket.code.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody @Valid UserCreationRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/verify")
	public ResponseEntity<String> verify(@RequestBody VerifyOtpRequest request) {
		return ResponseEntity.ok(authService.verifyAccount(request));
	}

	// API Đăng nhập
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	// API Quên mật khẩu - Bước 1: Yêu cầu OTP
	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
		return ResponseEntity.ok(authService.requestForgotPassword(request));
	}

	// API Quên mật khẩu - Bước 2: Đổi mật khẩu
	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
		return ResponseEntity.ok(authService.resetPassword(request));
	}
}
