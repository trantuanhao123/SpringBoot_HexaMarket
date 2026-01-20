package com.hexamarket.code.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.UserUpdateRequest;
import com.hexamarket.code.dto.response.UserResponse;
import com.hexamarket.code.entity.Role;
import com.hexamarket.code.entity.User;
import com.hexamarket.code.mapper.UserMapper;
import com.hexamarket.code.repository.RoleRepository;
import com.hexamarket.code.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UserResponse createUser(UserCreationRequest request) {
		// Kiểm tra username và email đã tồn tại chưa
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Username already exists");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email already exists");
		}
		// Mapping và Encoder
		// Tạo entity và mã hóa password
		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		// Gán roles cho user
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("Role USER not found"));
		user.setRoles(new HashSet<>(Set.of(userRole)));
		// Lưu User vào database
		return userMapper.toUserResponse(userRepository.save(user));
	}

	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream().map(userMapper::toUserResponse).collect(Collectors.toList());
	}

	public UserResponse getUserById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		return userMapper.toUserResponse(user);
	}

	public UserResponse getUserByUsername(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found with username: " + username));
		return userMapper.toUserResponse(user);
	}

	@Transactional
	public UserResponse updateUser(Long id, UserUpdateRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
		userMapper.updateUser(user, request);
		return userMapper.toUserResponse(userRepository.save(user));
	}

	@Transactional
	public String deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new RuntimeException("User not found with id: " + id);
		}
		userRepository.deleteById(id);
		return "User deleted successfully with id: " + id;
	}
}
