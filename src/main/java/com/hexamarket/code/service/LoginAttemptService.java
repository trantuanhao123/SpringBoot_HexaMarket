package com.hexamarket.code.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

	private final StringRedisTemplate redisTemplate;

	private static final String RATE_KEY = "login:rate:";
	private static final String BLACKLIST_KEY = "login:blacklist:";

	private static final int MAX_ATTEMPT = 5; // 5 lần / phút
	private static final int BLACKLIST_THRESHOLD = 10; // spam nặng
	private static final Duration RATE_TTL = Duration.ofMinutes(1);
	private static final Duration BLACKLIST_TTL = Duration.ofMinutes(5);

	public boolean isBlacklisted(String ip) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY + ip));
	}

	public long increaseAttempt(String ip) {
		String key = RATE_KEY + ip;

		Long count = redisTemplate.opsForValue().increment(key);
		if (count == 1) {
			redisTemplate.expire(key, RATE_TTL);
		}

		if (count >= BLACKLIST_THRESHOLD) {
			blacklist(ip);
		}

		return count;
	}

	public void blacklist(String ip) {
		redisTemplate.opsForValue().set(BLACKLIST_KEY + ip, "BLOCKED", BLACKLIST_TTL);
	}

	public boolean isRateLimited(long count) {
		return count > MAX_ATTEMPT;
	}
}
