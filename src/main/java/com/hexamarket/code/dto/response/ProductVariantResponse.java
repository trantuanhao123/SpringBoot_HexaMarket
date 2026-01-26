package com.hexamarket.code.dto.response;

import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

	private Long id;

	private Long productId; // FK để FE biết thuộc product nào

	private String sku;

	private BigDecimal price;

	/**
	 * Thuộc tính động từ JSONB VD: {"color": "Red", "size": "XL"}
	 */
	private Map<String, Object> attributes;

	private String imageUrl; // Ảnh riêng của variant
}
