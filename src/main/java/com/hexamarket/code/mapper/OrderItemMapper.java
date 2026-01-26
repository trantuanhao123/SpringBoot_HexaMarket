package com.hexamarket.code.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hexamarket.code.dto.response.OrderItemResponse;
import com.hexamarket.code.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
	@Mapping(target = "variantId", source = "variant.id")
	@Mapping(target = "sku", source = "variant.sku")
	@Mapping(target = "subTotal", expression = "java(calcSubTotal(orderItem))")
	OrderItemResponse toResponse(OrderItem orderItem);

	default BigDecimal calcSubTotal(OrderItem item) {
		return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
	}
}
