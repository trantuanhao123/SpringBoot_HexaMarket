package com.hexamarket.code.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hexamarket.code.service.LoginAttemptService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

	private final LoginAttemptService loginAttemptService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String path = request.getRequestURI();

		if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {

			String ip = getClientIP(request);
			// Check blacklist trước
			if (loginAttemptService.isBlacklisted(ip)) {
				response.setStatus(403);
				response.getWriter().write("Your IP is temporarily blocked due to suspicious activity.");
				return;
			}

			long attempts = loginAttemptService.increaseAttempt(ip);

			if (loginAttemptService.isRateLimited(attempts)) {
				response.setStatus(429);
				response.getWriter().write("Too many login attempts. Please try again later.");
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private String getClientIP(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null && !xfHeader.isEmpty()) {
			return xfHeader.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
