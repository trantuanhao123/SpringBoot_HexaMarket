package com.hexamarket.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.request.ForgotPasswordRequest;
import com.hexamarket.code.dto.request.LoginRequest;
import com.hexamarket.code.dto.request.RefreshTokenRequest;
import com.hexamarket.code.dto.request.ResetPasswordRequest;
import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.VerifyOtpRequest;
import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.dto.response.AuthResponse;
import com.hexamarket.code.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@RequestBody @Valid UserCreationRequest request) {
		return created(authService.register(request), "Register success. Please verify OTP.");
	}

	@PostMapping("/verify")
	public ResponseEntity<ApiResponse<String>> verify(@RequestBody VerifyOtpRequest request) {
		return ok(authService.verifyAccount(request));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
		return ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshTokenRequest request) {
		return ok(authService.refreshToken(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
		authService.logout(request);
		return okMessage("Logged out successfully");
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
		return ok(authService.requestForgotPassword(request));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
		return ok(authService.resetPassword(request));
	}
}
