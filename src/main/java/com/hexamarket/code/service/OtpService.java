package com.hexamarket.code.service;

import java.time.Duration;
import java.util.Random;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {
	private final StringRedisTemplate redisTemplate;
	private static final String OTP_PREFIX = "OTP:";

	// Lưu OTP trong 5 phút
	public String generateOtp(String email) {
		String otp = String.valueOf(new Random().nextInt(900000) + 100000);
		redisTemplate.opsForValue().set("OTP:" + email, otp, Duration.ofMinutes(5));
		return otp;
	}

	// Lấy otp lưu trong redis ra và so sánh với input
	public boolean validateOtp(String email, String otpInput) {
		String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + email);
		if (storedOtp != null && storedOtp.equals(otpInput)) {
			return true;
		}
		return false;
	}

	public void deleteOtp(String email) {
		redisTemplate.delete(OTP_PREFIX + email);
	}
}
