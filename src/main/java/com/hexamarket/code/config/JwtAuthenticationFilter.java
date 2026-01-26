package com.hexamarket.code.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private final TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = jwtUtils.extractTokenFromRequest(request);

		// Nếu không có token, cho qua để SecurityConfig xử lý
		// (401 hoặc cho phép tùy endpoint)
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// 1. Parse Token (JwtUtils sẽ tự throw exception nếu hết hạn hoặc sai chữ ký)
			var claims = jwtUtils.extractAllClaims(token);
			String jti = claims.getId();

			// 2. Check Blacklist (Redis) - Stateful check duy nhất
			if (tokenService.isTokenBlacklisted(jti)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
				return;
			}

//			if (tokenService.isTokenBlacklisted(jti)) {
//				log.warn("Token is blacklisted: {}", jti);
//				SecurityContextHolder.clearContext();
//				filterChain.doFilter(request, response); // Để ExceptionTranslationFilter xử lý
//				return;
//			}

			// 3. Tối ưu: Lấy User và Role trực tiếp từ Token (Không query DB)
//			String username = claims.getSubject();
//			var authorities = jwtUtils.extractAuthorities(token);
			Long userId = jwtUtils.extractUserId(token);
			var authorities = jwtUtils.extractAuthorities(token);
			// 4. Set Context
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				// Tạo đối tượng Authentication
				// Principal là username (String), Credentials là null (không cần pass),
				// Authorities lấy từ token
//				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
//						authorities);
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, null,
						authorities);
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}

		} catch (Exception e) {
			// Log lỗi nhưng KHÔNG return lỗi ngay, để filterChain tiếp tục chạy.
			// Khi đến cuối chain, nếu không có Authentication, Spring sẽ gọi
			// AuthenticationEntryPoint.
			log.error("JWT Authentication failed: {}", e.getMessage());
			SecurityContextHolder.clearContext(); // Xóa context cho chắc chắn
		}

		filterChain.doFilter(request, response);
	}

//	@Override
//	protected boolean shouldNotFilter(HttpServletRequest request) {
//		String path = request.getServletPath();
//
//		// Chỉ bỏ qua login / register
//		return path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/verify")
//				|| path.equals("/api/auth/forgot-password") || path.equals("/api/auth/reset-password");
//	}
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return path.startsWith("/api/auth/");
	}
}
