package com.hexamarket.code.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.hexamarket.code.dto.response.CartItemResponse;
import com.hexamarket.code.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
	@Mapping(target = "variantId", source = "variant.id")
	@Mapping(target = "productId", source = "variant.product.id")
	@Mapping(target = "productName", source = "variant.product.name")
	@Mapping(target = "sku", source = "variant.sku")
	@Mapping(target = "price", source = "variant.price")
	@Mapping(target = "imageUrl", source = "variant.imageUrl") // Nếu variant null thì có thể fallback về product
																// thumbnail
	@Mapping(target = "attributes", source = "variant.attributes")
	@Mapping(target = "quantity", source = "quantity")
	@Mapping(target = "totalPrice", expression = "java(calculateTotal(variant, quantity))")
	CartItemResponse toCartItemResponse(ProductVariant variant, Integer quantity);

	@Named("calculateTotal")
	default BigDecimal calculateTotal(ProductVariant variant, Integer quantity) {
		if (variant == null || variant.getPrice() == null || quantity == null) {
			return BigDecimal.ZERO;
		}
		return variant.getPrice().multiply(BigDecimal.valueOf(quantity));
	}
}
