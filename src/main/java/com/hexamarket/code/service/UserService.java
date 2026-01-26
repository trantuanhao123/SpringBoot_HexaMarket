package com.hexamarket.code.service;

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
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.UserMapper;
import com.hexamarket.code.repository.RoleRepository;
import com.hexamarket.code.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService extends BaseService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	/* ================= CREATE USER ================= */

	@Transactional
	public UserResponse createUser(UserCreationRequest request) {

		logStart("CREATE_USER", request.getUsername());

		notNull(request, ErrorCode.INVALID_REQUEST);
		notBlank(request.getUsername(), ErrorCode.USERNAME_INVALID);
		notBlank(request.getPassword(), ErrorCode.PASSWORD_INVALID);
		notBlank(request.getEmail(), ErrorCode.INVALID_REQUEST_DATA);

		require(!userRepository.existsByUsername(request.getUsername()), ErrorCode.USER_EXISTED);
		require(!userRepository.existsByEmail(request.getEmail()), ErrorCode.EMAIL_EXISTED);

		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setIsActive(true);

		Role role = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
		user.setRoles(Set.of(role));

		User saved = userRepository.save(user);

		logSuccess("CREATE_USER", saved.getId());
		return userMapper.toUserResponse(saved);
	}

	/* ================= READ ================= */

	public List<UserResponse> getAllUsers() {
		logStart("GET_ALL_USERS");
		List<UserResponse> users = userRepository.findAll().stream().map(userMapper::toUserResponse)
				.collect(Collectors.toList());
		logSuccess("GET_ALL_USERS", users.size());
		return users;
	}

	public UserResponse getUserById(Long id) {
		logStart("GET_USER_BY_ID", id);
		User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
		return userMapper.toUserResponse(user);
	}

	public UserResponse getUserByUsername(String username) {
		logStart("GET_USER_BY_USERNAME", username);
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
		return userMapper.toUserResponse(user);
	}

	/* ================= UPDATE ================= */

	@Transactional
	public UserResponse updateUser(Long id, UserUpdateRequest request) {

		logStart("UPDATE_USER", id);

		User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

		// Check email trùng
		if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
			require(!userRepository.existsByEmail(request.getEmail()), ErrorCode.EMAIL_EXISTED);
		}

		// Nếu đổi password → encode
//		if (request.getPassword() != null && !request.getPassword().isBlank()) {
//			user.setPassword(passwordEncoder.encode(request.getPassword()));
//		}

		userMapper.updateUser(user, request);

		User saved = userRepository.save(user);
		logSuccess("UPDATE_USER", id);

		return userMapper.toUserResponse(saved);
	}

	/* ================= DELETE (SOFT DELETE) ================= */

	@Transactional
	public String deleteUser(Long id) {

		logStart("DELETE_USER", id);

		User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

		// Soft delete thay vì xoá DB
		user.setIsActive(false);
		userRepository.save(user);

		logSuccess("DELETE_USER", id);
		return "User deactivated successfully with id: " + id;
	}
}
