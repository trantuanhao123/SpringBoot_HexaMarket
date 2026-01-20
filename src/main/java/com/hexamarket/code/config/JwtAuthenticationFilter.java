package com.hexamarket.code.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hexamarket.code.service.TokenService;
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
	private final TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = jwtUtils.extractTokenFromRequest(request);
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// 1. Parse & validate JWT (signature + exp)
			var claims = jwtUtils.extractAllClaims(token);
			String jti = claims.getId();

			// 2. Check if token is blacklisted
			if (tokenService.isTokenBlacklisted(jti)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked");
				return;
			}

			String username = claims.getSubject();

			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailService.loadUserByUsername(username);

				if (!jwtUtils.isTokenExpired(token)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (Exception e) {
			log.warn("JWT rejected: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
			return;
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();

		// Chỉ bỏ qua login / register
		return path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/verify")
				|| path.equals("/api/auth/forgot-password") || path.equals("/api/auth/reset-password");
	}
}
