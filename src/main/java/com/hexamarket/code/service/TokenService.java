package com.hexamarket.code.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final StringRedisTemplate redis;

	// PREFIX
	private static final String REFRESH_PREFIX = "auth:refresh:"; // key: auth:refresh:{jti}
	private static final String USER_REFRESH_SET_PREFIX = "auth:user-refresh-set:"; // set: user -> list of jti
	private static final String BLACKLIST_PREFIX = "auth:blacklist:"; // key: blacklisted access token

	// REFRESH TOKEN (ROTATION)

	public void storeRefreshToken(Long userId, String jti, long ttlMs) {

		// 1. Lưu refresh token tồn tại
		redis.opsForValue().set(REFRESH_PREFIX + jti, "1", ttlMs, TimeUnit.MILLISECONDS);

		// 2. Lưu JTI vào set của user
		String userSetKey = USER_REFRESH_SET_PREFIX + userId;
		redis.opsForSet().add(userSetKey, jti);

		// 3. TTL cho set (đồng bộ với refresh token)
		Long currentTtl = redis.getExpire(userSetKey, TimeUnit.MILLISECONDS);

		if (currentTtl == null || currentTtl < ttlMs) {
			redis.expire(userSetKey, ttlMs, TimeUnit.MILLISECONDS);
		}

	}

	public boolean existsRefreshToken(String jti) {
		return Boolean.TRUE.equals(redis.hasKey(REFRESH_PREFIX + jti));
	}

	public void revokeRefreshToken(String jti) {
		redis.delete(REFRESH_PREFIX + jti);
	}

	public void removeRefreshFromUserSet(Long userId, String jti) {
		redis.opsForSet().remove(USER_REFRESH_SET_PREFIX + userId, jti);
	}

	// LOGOUT

	public void logoutSingle(Long userId, String refreshJti, String accessJti, long accessTtlMs) {

		// Xóa refresh token
		revokeRefreshToken(refreshJti);

		// Gỡ khỏi set user
		redis.opsForSet().remove(USER_REFRESH_SET_PREFIX + userId, refreshJti);

		// Blacklist access token
		blacklistToken(accessJti, accessTtlMs);
	}

	public void logoutAll(Long userId) {

		String userSetKey = USER_REFRESH_SET_PREFIX + userId;

		// Lấy toàn bộ refresh JTI của user (STRING, không deserialize JSON)
		Set<String> jtIs = redis.opsForSet().members(userSetKey);

		if (jtIs != null && !jtIs.isEmpty()) {
			for (String jti : jtIs) {
				redis.delete(REFRESH_PREFIX + jti);
			}
		}

		// Xóa luôn set
		redis.delete(userSetKey);
	}

	// ACCESS TOKEN BLACKLIST

	public void blacklistToken(String jti, long ttlMs) {
		redis.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttlMs, TimeUnit.MILLISECONDS);
	}

	public boolean isTokenBlacklisted(String jti) {
		return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + jti));
	}
}