package com.hexamarket.code.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.hexamarket.code.dto.response.CartItemResponse;
import com.hexamarket.code.entity.ProductVariant;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.CartItemMapper;
import com.hexamarket.code.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService extends BaseService {

	private final StringRedisTemplate redis;
	private final CartItemMapper cartItemMapper;
	private final ProductVariantRepository variantRepository;

	private static final String CART_PREFIX = "hexacart:cart:";
	private static final int MAX_QUANTITY_PER_ITEM = 50;

	// ADD TO CART

	public void addToCart(Long userId, Long variantId, Integer quantity) {

		logStart("ADD_TO_CART", userId, variantId, quantity);

		require(quantity > 0, ErrorCode.INVALID_QUANTITY);
		require(quantity <= MAX_QUANTITY_PER_ITEM, ErrorCode.INVALID_QUANTITY);
		require(variantRepository.existsById(variantId), ErrorCode.PRODUCT_NOT_FOUND);

		String key = CART_PREFIX + userId;

		// Atomic tăng số lượng
		Long newQty = redis.opsForHash().increment(key, variantId.toString(), quantity);

		// Nếu vượt max → rollback lại
		if (newQty > MAX_QUANTITY_PER_ITEM) {
			redis.opsForHash().increment(key, variantId.toString(), -quantity);
			throw new AppException(ErrorCode.INVALID_QUANTITY);
		}

		// Tự xoá cart sau 7 ngày không hoạt động
		redis.expire(key, Duration.ofDays(7));

		logSuccess("ADD_TO_CART", variantId, newQty);
	}

	// UPDATE ITEM

	public void updateCartItem(Long userId, Long variantId, Integer quantity) {

		logStart("UPDATE_CART_ITEM", userId, variantId, quantity);

		if (quantity <= 0) {
			removeItem(userId, variantId);
			return;
		}

		require(quantity <= MAX_QUANTITY_PER_ITEM, ErrorCode.INVALID_QUANTITY);
		require(variantRepository.existsById(variantId), ErrorCode.PRODUCT_NOT_FOUND);

		redis.opsForHash().put(CART_PREFIX + userId, variantId.toString(), quantity.toString());

		logSuccess("UPDATE_CART_ITEM", variantId);
	}

	// REMOVE

	public void removeItem(Long userId, Long variantId) {
		String key = CART_PREFIX + userId;

		// delete trả về 1 nếu xóa thành công, 0 nếu không tồn tại
		Long result = redis.opsForHash().delete(key, variantId.toString());

		// Nếu kết quả là 0 -> Bắn lỗi Not Found
		if (result == 0) {
			throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
			// Hoặc tạo thêm ErrorCode.CART_ITEM_NOT_FOUND nếu muốn rõ nghĩa hơn
		}
	}

	public void clearCart(Long userId) {
		redis.delete(CART_PREFIX + userId);
	}

	// GET CART

	public List<CartItemResponse> getCart(Long userId) {

		String key = CART_PREFIX + userId;
		Map<Object, Object> cartData = redis.opsForHash().entries(key);

		if (cartData.isEmpty())
			return List.of();

		// Lấy danh sách variantId
		List<Long> variantIds = cartData.keySet().stream().map(k -> Long.valueOf(k.toString())).toList();

		// Query DB 1 lần duy nhất
		List<ProductVariant> variants = variantRepository.findAllById(variantIds);

		return variants.stream().map(variant -> {
			Integer quantity = Integer.valueOf(cartData.get(variant.getId().toString()).toString());
			return cartItemMapper.toCartItemResponse(variant, quantity);
		}).toList();
	}
}
