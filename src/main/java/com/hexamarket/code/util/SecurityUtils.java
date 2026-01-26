package com.hexamarket.code.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
	private SecurityUtils() {
	}

	public static Long getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}

		Object principal = auth.getPrincipal();

		if (principal instanceof Long userId) {
			return userId;
		}

		throw new RuntimeException("Invalid authentication principal");
	}
}
