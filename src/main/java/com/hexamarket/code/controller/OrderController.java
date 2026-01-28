package com.hexamarket.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.constant.OrderStatus;
import com.hexamarket.code.dto.request.OrderRequest;
import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.dto.response.OrderResponse;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.service.OrderService;
import com.hexamarket.code.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController extends BaseController {

	private final OrderService orderService;

	// Chỉ user được tạo đơn hàng
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/checkout")
	public ResponseEntity<ApiResponse<OrderResponse>> checkout(@RequestBody @Valid OrderRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		return created(orderService.checkout(userId, request), "Order placed successfully");
	}

	// Chỉ admin được cập nhật trạng thái đơn hàng
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{id}/status")
	public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(@PathVariable Long id,
			@RequestParam OrderStatus status) {

		// Validate: status không được null
		if (status == null) {
			throw new AppException(ErrorCode.INVALID_REQUEST);
		}

		return ResponseEntity.ok(ApiResponse.<OrderResponse>builder().result(orderService.updateStatus(id, status))
				.message("Cập nhật trạng thái thành công").build());
	}
}
