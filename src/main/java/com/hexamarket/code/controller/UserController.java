package com.hexamarket.code.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.UserUpdateRequest;
import com.hexamarket.code.dto.response.UserResponse;
import com.hexamarket.code.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// Route tạo user
	@PostMapping
	public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
	}

	// Route lấy danh sách user / 1 user
	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getUserById(id));
	}

	// Profile
	@GetMapping("/profile")
	public ResponseEntity<UserResponse> profile(Authentication authentication) {
		return ResponseEntity.ok(userService.getUserByUsername(authentication.getName()));
	}

	// Route cập nhật thông tin
	@PutMapping("/{id}")
	public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
			@RequestBody @Valid UserUpdateRequest request) {
		return ResponseEntity.ok(userService.updateUser(id, request));
	}

	// Route xóa user
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		String message = userService.deleteUser(id);
		return ResponseEntity.ok(message);
	}
}
