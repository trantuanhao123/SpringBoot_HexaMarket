package com.hexamarket.code.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductSearchRequest {
	private String name;
	private Long categoryId;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private String attributeKey; // VD: "ram"
	private String attributeValue; // VD: "8GB"
}
