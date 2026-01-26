package com.hexamarket.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.hexamarket.code.constant.OrderStatus;
import com.hexamarket.code.dto.response.OrderResponse;
import com.hexamarket.code.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
	@Mapping(target = "orderId", source = "id")
	@Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
	@Mapping(target = "paymentStatus", source = "paymentStatus")
	@Mapping(target = "items", source = "items")
	OrderResponse toOrderResponse(Order order);

	@Named("mapStatus")
	default String mapStatus(OrderStatus status) {
		return status != null ? status.name() : null;
	}
}
