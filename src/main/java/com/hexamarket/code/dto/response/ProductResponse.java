package com.hexamarket.code.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductResponse {
	private Long id;
	private String name;
	private String slug;
	private String description;
	private String thumbnail;
	private BigDecimal basePrice;
	private String categoryName;
	private List<ProductVariantResponse> variants;
}
