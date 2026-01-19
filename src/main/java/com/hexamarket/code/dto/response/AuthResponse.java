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
	// Mở rộng thêm: refreshToken, expirationTime,userType...
}
