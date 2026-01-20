package com.hexamarket.code.service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final StringRedisTemplate redis;

	// Prefix cho key lưu trạng thái token (Value: "valid")
	private static final String REFRESH_PREFIX = "auth:refresh:";

	// Prefix cho key lưu danh sách JTI của user (Type: Set)
	private static final String USER_REFRESH_SET_PREFIX = "auth:user-refresh-set:";

	// Prefix cho blacklist access token
	private static final String BLACKLIST_PREFIX = "auth:blacklist:";

	// REFRESH TOKEN (Rotation)

	// 1. Lưu refresh token theo jti
	public void storeRefreshToken(Long userId, String jti, long ttlMs) {
		// Lưu trạng thái sống của token để validate
		redis.opsForValue().set(REFRESH_PREFIX + jti, "valid", Duration.ofMillis(ttlMs));
		// Thêm JTI vào danh sách của user (để logout all)
		// Redis set tự động loại bỏ trùng lặp
		String userSetKey = USER_REFRESH_SET_PREFIX + userId;
		redis.opsForSet().add(userSetKey, jti);
		// Cập nhật thời gian sống cho set user (bằng hoặc dài hơn ttl của token)
		redis.expire(userSetKey, Duration.ofMillis(ttlMs));
	}

	// 2. Kiểm tra refresh token còn hợp lệ không
	public boolean existsRefreshToken(String jti) {
		return Boolean.TRUE.equals(redis.hasKey(REFRESH_PREFIX + jti));
	}

	// 3. Revoke 1 refresh token (khi xoay vòng token)
	public void revokeRefreshToken(String jti) {
		// Xóa key xác thực
		redis.delete(REFRESH_PREFIX + jti);

		// Lưu ý: Ta không nhất thiết phải xóa JTI khỏi Set của User ngay lập tức
		// vì nó tốn thêm 1 query và nếu key xác thực đã mất thì JTI trong Set vô dụng.
		// Nhưng nếu muốn sạch sẽ tuyệt đối, bạn có thể truyền thêm userId vào hàm này
		// để xóa trong Set.
	}

	// LOGOUT

	// Logout 1 device (revoke refresh + blacklist access)
	public void logoutSingle(Long userId, String refreshJti, String accessJti, long accessTtlMs) {
		// Xóa key xác thực refresh
		revokeRefreshToken(refreshJti);

		// Xóa JTI khỏi danh sách của User
		redis.opsForSet().remove(USER_REFRESH_SET_PREFIX + userId, refreshJti);

		// Blacklist access token hiện tại
		blacklistToken(accessJti, accessTtlMs);
	}

	// Logout tất cả device
	public void logoutAll(Long userId) {
		String userSetKey = USER_REFRESH_SET_PREFIX + userId;

		// B1: Lấy toàn bộ JTI đang có trong Set của user
		Set<String> jtis = redis.opsForSet().members(userSetKey);

		if (jtis != null && !jtis.isEmpty()) {
			// B2: Tạo danh sách các key "auth:refresh:{jti}" cần xóa
			Set<String> refreshKeysToDelete = jtis.stream().map(jti -> REFRESH_PREFIX + jti)
					.collect(Collectors.toSet());

			// B3: Xóa hàng loạt key xác thực (O(N) với N là số token của user -> rất nhỏ)
			redis.delete(refreshKeysToDelete);
		}

		// B4: Xóa luôn cái Set danh sách của user
		redis.delete(userSetKey);
	}

	// ACCESS TOKEN BLACKLIST

	public void blacklistToken(String jti, long ttlMs) {
		redis.opsForValue().set(BLACKLIST_PREFIX + jti, "true", Duration.ofMillis(ttlMs));
	}

	public boolean isTokenBlacklisted(String jti) {
		return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + jti));
	}
}
