package com.hexamarket.code.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
	private Long orderId;
	private String status;
	private String paymentStatus;
	private BigDecimal totalAmount;
	private LocalDateTime createdAt;
	private List<OrderItemResponse> items;
}
