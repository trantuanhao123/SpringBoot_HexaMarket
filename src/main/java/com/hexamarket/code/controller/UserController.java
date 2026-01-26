package com.hexamarket.code.controller;

import java.util.List;

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
import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.dto.response.UserResponse;
import com.hexamarket.code.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid UserCreationRequest request) {
		return created(userService.createUser(request), "User created successfully");
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
		return ok(userService.getAllUsers());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
		return ok(userService.getUserById(id));
	}

	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<UserResponse>> profile(Authentication authentication) {
		Long userId = (Long) authentication.getPrincipal();
		return ok(userService.getUserById(userId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
			@RequestBody @Valid UserUpdateRequest request) {
		return ok(userService.updateUser(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return deleted("User deleted successfully");
	}
}
