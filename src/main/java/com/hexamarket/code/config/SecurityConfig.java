package com.hexamarket.code.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final LoginRateLimitFilter loginRateLimitFilter;

	// Cấu hình bcrypt với độ mạnh là 10
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

	// Bean này cần thiết để Inject vào Auth Controller (dùng để login)
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	// Bean cấu hình CORS
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("*")); // Trong dev thì allow all, prod nên sửa lại domain cụ thể
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Tắt CSRF vì dùng JWT (Stateless)
				.csrf(csrf -> csrf.disable())
				// Cấu hình CORS
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// 1. Chuyển sang stateless (Không lưu session trên server)
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// 2. Xử lý lỗi 401 trả về JSON thay vì HTML login mặc định
				.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				// 3. Thêm filter giới hạn tần suất login (Chèn trước UsernamePassword)
				.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				// 4. Thêm filter JWT (Cũng chèn trước UsernamePassword)
				// Kết quả thứ tự chạy: RateLimit -> JWT -> UsernamePassword
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				// 5. Phân quyền
				.authorizeHttpRequests(auth -> auth
						// --- SWAGGER UI (Tài liệu API) ---
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
						// --- AUTHENTICATION (Login/Register/RefreshToken) ---
						.requestMatchers("/api/auth/**").permitAll() // Đã xóa bớt 1 cái trùng
						.requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Tạo user mới
						// Route này bảo mật bằng HMAC Signature trong code, không cần JWT
						.requestMatchers("/api/payment/webhook").permitAll()
						// --- CÁC ROUTE CÒN LẠI ---
						// Bắt buộc phải có Token hợp lệ
						.anyRequest().authenticated());

		return http.build();
	}
}