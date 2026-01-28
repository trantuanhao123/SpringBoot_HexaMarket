package com.hexamarket.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController extends BaseController {

	private final PaymentService paymentService;

	// Webhook từ Payment Gateway Không trả data business, chỉ xác nhận đã nhận
	@PreAuthorize("permitAll()")
	@PostMapping("/webhook")
	public ResponseEntity<ApiResponse<String>> paymentWebhook(@RequestParam Long orderId, @RequestParam String status,
			@RequestParam String signature) {

		paymentService.processWebhook(orderId, status, signature);

		return ok("Webhook processed successfully");
	}
}
