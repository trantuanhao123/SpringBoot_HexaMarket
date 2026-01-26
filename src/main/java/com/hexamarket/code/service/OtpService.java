package com.hexamarket.code.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {
	private final StringRedisTemplate redis;
	private static final String OTP_PREFIX = "OTP:";
	private static final SecureRandom secureRandom = new SecureRandom();

	// Lưu OTP trong 5 phút
	public String generateOtp(String email) {
		String otp = String.valueOf(100000 + secureRandom.nextInt(900000));
		redis.opsForValue().set("OTP:" + email, otp, Duration.ofMinutes(5));
		return otp;
	}

	// Lấy otp lưu trong redis ra và so sánh với input
	public boolean validateOtp(String email, String otpInput) {
		String storedOtp = redis.opsForValue().get(OTP_PREFIX + email);
		if (storedOtp != null && storedOtp.equals(otpInput)) {
			redis.delete(OTP_PREFIX + email);// Xóa ngay sau khi dùng (One-time use)
			return true;
		}
		return false;
	}

	public void deleteOtp(String email) {
		redis.delete(OTP_PREFIX + email);
	}
}
