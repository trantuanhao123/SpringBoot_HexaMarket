package com.hexamarket.code.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.hexamarket.code.constant.OrderStatus;
import com.hexamarket.code.dto.response.OrderResponse;
import com.hexamarket.code.entity.Order;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {

	@Mapping(target = "orderId", source = "id")
	@Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
	@Mapping(target = "paymentStatus", source = "paymentStatus")

	// MapStruct sẽ tự động gọi OrderItemMapper.toResponse() cho từng phần tử trong
	// list này
	@Mapping(target = "items", source = "items")
	@Mapping(target = "email", source = "user.email")
	OrderResponse toOrderResponse(Order order);

	@Named("mapStatus")
	default String mapStatus(OrderStatus status) {
		return status != null ? status.name() : null;
	}
}