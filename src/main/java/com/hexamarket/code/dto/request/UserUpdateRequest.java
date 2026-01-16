package com.hexamarket.code.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

	// Không cho phép sửa username password
	@Size(min = 2, message = "Full name must be at least 2 characters")
	private String fullName;
	@Email(message = "Invalid email format")
	private String email;
	@Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
	private String phoneNumber;
	// Admin có thể update trạng thái user, nhưng user thường thì không.
	// Tạm thời mình để ở đây, sau này dùng @PreAuthorize để chặn nếu cần.
	private Boolean isActive;
}