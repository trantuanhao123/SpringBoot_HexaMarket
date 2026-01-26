package com.hexamarket.code.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
	Long variantId;
	Long productId;
	String productName;
	String sku;
	BigDecimal price; // Giá của variant
	Integer quantity; // Lấy từ Redis
	String imageUrl; // Ảnh của variant hoặc product
	Object attributes; // JSONB attributes (Size, Color...)
	BigDecimal totalPrice; // price * quantity (tính toán thêm cho tiện FE)
}
