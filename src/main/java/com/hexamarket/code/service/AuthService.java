package com.hexamarket.code.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hexamarket.code.dto.request.ForgotPasswordRequest;
import com.hexamarket.code.dto.request.LoginRequest;
import com.hexamarket.code.dto.request.RefreshTokenRequest;
import com.hexamarket.code.dto.request.ResetPasswordRequest;
import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.VerifyOtpRequest;
import com.hexamarket.code.dto.response.AuthResponse;
import com.hexamarket.code.entity.Role;
import com.hexamarket.code.entity.User;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.UserMapper;
import com.hexamarket.code.repository.RoleRepository;
import com.hexamarket.code.repository.UserRepository;
import com.hexamarket.code.util.JwtUtils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService extends BaseService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final OtpService otpService;
	private final EmailService emailService;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final TokenService tokenService;

	// Đăng ký (Tạo user isActive = false -> Gửi OTP)
	@Transactional
	public String register(UserCreationRequest request) {
		logStart("REGISTER", request.getUsername(), request.getEmail());
		require(!userRepository.existsByUsername(request.getUsername()), ErrorCode.USER_EXISTED);
		require(!userRepository.existsByEmail(request.getEmail()), ErrorCode.EMAIL_EXISTED);
		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setIsActive(false);
		Role role = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
		user.setRoles(new HashSet<>(Set.of(role)));
		userRepository.save(user);
		String otp = otpService.generateOtp(user.getEmail());
		emailService.sendOtpEmail(user.getEmail(), otp);
		logSuccess("REGISTER", user.getId());
		return "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.";
	}

	// Xác thực OTP (Update isActive = true)
	@Transactional
	public String verifyAccount(VerifyOtpRequest request) {
		logStart("VERIFY_ACCOUNT", request.getEmail());
		require(otpService.validateOtp(request.getEmail(), request.getOtp()), ErrorCode.INVALID_OTP);
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
		require(!Boolean.TRUE.equals(user.getIsActive()), ErrorCode.ACCOUNT_ALREADY_ACTIVE);
		user.setIsActive(true);
		otpService.deleteOtp(request.getEmail());
		logSuccess("VERIFY_ACCOUNT", user.getId());
		return "Xác thực thành công!";
	}

	// Đăng nhập
//	public AuthResponse login(LoginRequest request) {
//		// AuthenticationManager sẽ tự động gọi UserDetailService để check user/pass
//		Authentication authentication = authenticationManager
//				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
//		// Nếu vượt qua dòng trên nghĩa là đăng nhập thành công
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//		// Lấy thông tin user để tạo token
////		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
////		User user = userRepository.findByUsername(userDetails.getUsername())
////				.orElseThrow(() -> new RuntimeException("User không tồn tại"));
////		String accessToken = jwtUtils.generateAccessToken(userDetails);
//		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//		User user = userRepository.findByUsername(userDetails.getUsername())
//				.orElseThrow(() -> new RuntimeException("User không tồn tại"));
//		String accessToken = jwtUtils.generateAccessToken(user);
//
//		String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
//		Claims refreshClaims = jwtUtils.extractAllClaims(refreshToken);
//		String refreshJti = refreshClaims.getId();
//		tokenService.storeRefreshToken(user.getId(), refreshJti, jwtUtils.getRefreshTokenExpiration());
//		return AuthResponse.builder().token(accessToken).refreshToken(refreshToken)
//				.expiresIn(jwtUtils.getAccessTokenExpiration()).build();
//	}
	public AuthResponse login(LoginRequest request) {
		logStart("LOGIN", request.getUsername());
		Authentication auth = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(auth);
		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
		require(Boolean.TRUE.equals(user.getIsActive()), ErrorCode.ACCOUNT_NOT_ACTIVE);
		String accessToken = jwtUtils.generateAccessToken(user);
		String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
		Claims claims = jwtUtils.extractAllClaims(refreshToken);
		tokenService.storeRefreshToken(user.getId(), claims.getId(), jwtUtils.getRefreshTokenExpiration());
		logSuccess("LOGIN", user.getId());
		return AuthResponse.builder().token(accessToken).refreshToken(refreshToken)
				.expiresIn(jwtUtils.getAccessTokenExpiration()).build();
	}

	// Refresh token rotation
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		logStart("REFRESH_TOKEN");

		jwtUtils.validateToken(request.getRefreshToken());
		Claims claims = jwtUtils.extractAllClaims(request.getRefreshToken());

		String oldJti = claims.getId();
		String username = claims.getSubject();

		require(tokenService.existsRefreshToken(oldJti), ErrorCode.REFRESH_TOKEN_EXPIRED);

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

		// Rotation
		tokenService.revokeRefreshToken(oldJti);
		tokenService.removeRefreshFromUserSet(user.getId(), oldJti);

		String newAccess = jwtUtils.generateAccessToken(user);
		String newRefresh = jwtUtils.generateRefreshToken(username);

		Claims newClaims = jwtUtils.extractAllClaims(newRefresh);
		tokenService.storeRefreshToken(user.getId(), newClaims.getId(), jwtUtils.getRefreshTokenExpiration());

		logSuccess("REFRESH_TOKEN", user.getId());
		return AuthResponse.builder().token(newAccess).refreshToken(newRefresh)
				.expiresIn(jwtUtils.getAccessTokenExpiration()).build();
	}

	// Đăng xuất
//	public void logout(HttpServletRequest request) {
//		String accessToken = jwtUtils.extractTokenFromRequest(request);
//		if (accessToken == null)
//			return;
//		// Parse access token
//		Claims accessClaims = jwtUtils.extractAllClaims(accessToken);
//		String accessJti = accessClaims.getId();
//		long accessTtl = jwtUtils.getRemainingTime(accessToken);
//		String username = accessClaims.getSubject();
//		// Blacklist access token
//		tokenService.blacklistToken(accessJti, accessTtl);
//
//		// Logout ALL: revoke toàn bộ refresh token của user
//		User user = userRepository.findByUsername(username)
//				.orElseThrow(() -> new RuntimeException("User không tồn tại"));
//
//		tokenService.logoutAll(user.getId());
//	}
	public void logout(HttpServletRequest request) {
		logStart("LOGOUT");

		String accessToken = jwtUtils.extractTokenFromRequest(request);
		if (accessToken == null)
			return;

		Claims claims = jwtUtils.extractAllClaims(accessToken);
		String accessJti = claims.getId();
		long ttl = jwtUtils.getRemainingTimeAllowExpired(accessToken);

		tokenService.blacklistToken(accessJti, ttl);

		logSuccess("LOGOUT", accessJti);
	}

	// Quên mật khẩu (Bước 1: Gửi OTP)
	public String requestForgotPassword(ForgotPasswordRequest request) {
		logStart("FORGOT_PASSWORD", request.getEmail());

		userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

		String otp = otpService.generateOtp(request.getEmail());
		emailService.sendOtpEmail(request.getEmail(), otp);

		logSuccess("FORGOT_PASSWORD");
		return "OTP đã được gửi.";
	}

	// Quên mật khẩu (Bước 2: Reset Password)
	@Transactional
	public String resetPassword(ResetPasswordRequest request) {
		logStart("RESET_PASSWORD", request.getEmail());

		require(request.getNewPassword().equals(request.getConfirmPassword()), ErrorCode.INVALID_KEY);
		require(otpService.validateOtp(request.getEmail(), request.getOtp()), ErrorCode.INVALID_OTP);

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		otpService.deleteOtp(request.getEmail());

		logSuccess("RESET_PASSWORD", user.getId());
		return "Đặt lại mật khẩu thành công.";
	}

}
