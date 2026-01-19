package com.hexamarket.code.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hexamarket.code.dto.request.ForgotPasswordRequest;
import com.hexamarket.code.dto.request.LoginRequest;
import com.hexamarket.code.dto.request.ResetPasswordRequest;
import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.VerifyOtpRequest;
import com.hexamarket.code.dto.response.AuthResponse;
import com.hexamarket.code.entity.Role;
import com.hexamarket.code.entity.User;
import com.hexamarket.code.mapper.UserMapper;
import com.hexamarket.code.repository.RoleRepository;
import com.hexamarket.code.repository.UserRepository;
import com.hexamarket.code.util.JwtUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final OtpService otpService;
	private final EmailService emailService;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;

	// Đăng ký (Tạo user isActive = false -> Gửi OTP)
	@Transactional
	public String register(UserCreationRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Username đã tồn tại");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email đã tồn tại");
		}
		// 1. Map DTO sang Entity
		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		// 2. Phòng hờ mapper sai giá trị
		user.setIsActive(false);
		// 3. Gán role mặc định
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy Role USER"));
		user.setRoles(new HashSet<>(Set.of(userRole)));
		// 4. Lưu vào DB
		userRepository.save(user);
		// 5. Sinh OTP và gửi email
		String otp = otpService.generateOtp(user.getEmail());
		emailService.sendOtpEmail(user.getEmail(), otp);
		return "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.";
	}

	// Xác thực OTP (Update isActive = true)
	@Transactional
	public String verifyAccount(VerifyOtpRequest request) {
		// 1. Check OTP từ redis
		boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
		if (!isValid) {
			throw new RuntimeException("Mã OTP không đúng hoặc đã hết hạn");
		}
		// 2. Lấy User từ DB
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User không tồn tại"));
		// 3. Kích hoạt tài khoản
		if (user.getIsActive()) {
			return "Tài khoản này đã được kích hoạt trước đó";
		}
		user.setIsActive(true);
		userRepository.save(user);
		otpService.deleteOtp(request.getEmail());
		return "Xác thực thành công! Bạn có thể đăng nhập ngay bây giờ.";
	}

	// Đăng nhập
	public AuthResponse login(LoginRequest request) {
		// AuthenticationManager sẽ tự động gọi UserDetailService để check user/pass +
		// encoder
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		// Nếu vượt qua dòng trên nghĩa là đăng nhập thành công
		SecurityContextHolder.getContext().setAuthentication(authentication);
		// Lấy thông tin user để tạo token
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String jwtToken = jwtUtils.generateToken(userDetails);
		return AuthResponse.builder().token(jwtToken).build();
	}

	// Quên mật khẩu (Bước 1: Gửi OTP)
	public String requestForgotPassword(ForgotPasswordRequest request) {
		// Kiểm tra email tồn tại
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("Email chưa được đăng ký trong hệ thống"));
		// Tái sử dụng logic sinh OTP và lưu Redis
		String otp = otpService.generateOtp(user.getEmail());
		// Tái sử dụng logic gửi mail
		emailService.sendOtpEmail(user.getEmail(), otp);
		return "Mã xác thực đã được gửi đến email của bạn.";
	}

	// Quên mật khẩu (Bước 2: Reset Password)
	@Transactional // Quan trọng để rollback nếu có lỗi
	public String resetPassword(ResetPasswordRequest request) {
		// 1. Validate Password khớp nhau
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new RuntimeException("Mật khẩu xác nhận không khớp");
		}
		// 2. Validate OTP từ Redis
		boolean isValidOtp = otpService.validateOtp(request.getEmail(), request.getOtp());
		if (!isValidOtp) {
			throw new RuntimeException("Mã OTP không đúng hoặc đã hết hạn");
		}
		// 3. Lấy user và cập nhật mật khẩu mới
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User không tồn tại"));
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		otpService.deleteOtp(request.getEmail());
		return "Đặt lại mật khẩu thành công";
	}
}
