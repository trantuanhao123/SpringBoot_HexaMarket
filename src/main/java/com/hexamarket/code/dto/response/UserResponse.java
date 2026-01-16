package com.hexamarket.code.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class UserResponse {
	// Properties
	// Không trả về password
	private Long id;
	private String username;
	private String email;
	private String fullName;
	private String phoneNumber;
	private boolean isActive;
	// Chỉ trả về tên các role thay vì Object Role đầy đủ
	private Set<String> roles;
	private LocalDateTime createdAt;
}
