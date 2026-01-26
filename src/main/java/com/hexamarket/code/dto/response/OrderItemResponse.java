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
public class OrderItemResponse {

	private Long variantId;
	private String productName;
	private String sku;
	private BigDecimal price;
	private Integer quantity;
	private BigDecimal subTotal;
}
