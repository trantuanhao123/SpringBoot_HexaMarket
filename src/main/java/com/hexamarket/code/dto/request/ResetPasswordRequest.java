package com.hexamarket.code.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
	@NotBlank(message = "Email is required")
	private String email;
	@NotBlank(message = "OTP is required")
	private String otp;
	@NotBlank(message = "New password is required")
	@Size(min = 6, message = "Password must be at least 6 characters")
	private String newPassword;
	@NotBlank(message = "Confirm password is required")
	private String confirmPassword;

}
