package com.hexamarket.code.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.request.CartItemRequest;
import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.dto.response.CartItemResponse;
import com.hexamarket.code.service.CartService;
import com.hexamarket.code.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController extends BaseController {

	private final CartService cartService;

	// 1. Thêm sản phẩm vào giỏ
	@PostMapping("/add")
	public ResponseEntity<ApiResponse<Void>> addToCart(@RequestBody @Valid CartItemRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		cartService.addToCart(userId, request.getVariantId(), request.getQuantity());
		return okMessage("Product added to cart successfully");
	}

	// 2. Lấy danh sách giỏ hàng
	@GetMapping
	public ResponseEntity<ApiResponse<List<CartItemResponse>>> getMyCart() {
		Long userId = SecurityUtils.getCurrentUserId();
		return ok(cartService.getCart(userId));
	}

	// 3. Cập nhật số lượng (Dùng PUT vì cập nhật state của item)
	@PutMapping("/update")
	public ResponseEntity<ApiResponse<Void>> updateCartItem(@RequestBody @Valid CartItemRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		cartService.updateCartItem(userId, request.getVariantId(), request.getQuantity());
		return okMessage("Cart item updated successfully");
	}

	// 4. Xóa một sản phẩm khỏi giỏ
	@DeleteMapping("/remove/{variantId}")
	public ResponseEntity<ApiResponse<Void>> removeCartItem(@PathVariable Long variantId) {
		Long userId = SecurityUtils.getCurrentUserId();
		cartService.removeItem(userId, variantId);
		return okMessage("Item removed from cart");
	}

	// 5. Xóa toàn bộ giỏ hàng
	@DeleteMapping("/clear")
	public ResponseEntity<ApiResponse<Void>> clearCart() {
		Long userId = SecurityUtils.getCurrentUserId();
		cartService.clearCart(userId);
		return okMessage("Cart cleared successfully");
	}
}