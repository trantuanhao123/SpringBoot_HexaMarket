package com.hexamarket.code.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hexamarket.code.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;
	private final UserDetailService userDetailService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 1. Lấy token từ header
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		// Bỏ chữ Bearer
		jwt = authHeader.substring(7);
		try {
			username = jwtUtils.extractUsername(jwt);
			// Nếu có username và chưa được xác thực trong Context
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = this.userDetailService.loadUserByUsername(username);
				// Kiểm tra tính hợp lệ của token
				if (jwtUtils.isTokenValid(jwt, userDetails)) {
					// Tạo đối tượng Authentication
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					// Set vào Security Context --> Request này đã được xác thực
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}

			}
		} catch (Exception e) {
			log.error("Cannot set user authentication: {}", e.getMessage());

		}
		filterChain.doFilter(request, response);
	}
}
