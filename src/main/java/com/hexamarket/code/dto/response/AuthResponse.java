package com.hexamarket.code.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private String refreshToken;
	// Giá trị mặc đinh = "Bearer"
	@Builder.Default
	private String tokenType = "Bearer";
	// Thời gian hết hạn (milliseconds)
	private long expiresIn;
}
