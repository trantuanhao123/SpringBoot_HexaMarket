package com.hexamarket.code.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexamarket.code.constant.OrderStatus;
import com.hexamarket.code.entity.Order;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.OrderMapper;
import com.hexamarket.code.repository.OrderRepository;
import com.hexamarket.code.util.HmacUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService extends BaseService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final StringRedisTemplate redisTemplate;
	private final NotificationService notificationService;

	@Value("${security.payment.secret}")
	private String paymentSecret;

	@Transactional
	public void processWebhook(Long orderId, String status, String receivedSignature) {

		logStart("PAYMENT_WEBHOOK", orderId, status);

		/* ===== 1. VALIDATE INPUT ===== */
		notNull(orderId, ErrorCode.INVALID_REQUEST_DATA);
		notBlank(status, ErrorCode.INVALID_REQUEST_DATA);
		notBlank(receivedSignature, ErrorCode.INVALID_REQUEST_TOKEN);

		/* ===== 2. VERIFY SIGNATURE ===== */
		String data = "orderId=" + orderId + "&status=" + status;
		String calculatedSignature = HmacUtils.hmacSHA512(paymentSecret, data);

		require(calculatedSignature.equals(receivedSignature), ErrorCode.UNAUTHENTICATED);

		/* ===== 3. LOAD ORDER ===== */
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

		/* ===== 4. BUSINESS LOGIC ===== */
		if ("SUCCESS".equals(status)) {

			require("UNPAID".equals(order.getPaymentStatus()), ErrorCode.INVALID_STATE_TRANSITION);

			order.setPaymentStatus("PAID");
			order.setStatus(OrderStatus.CONFIRMED);
			orderRepository.save(order);

			redisTemplate.delete("order:timeout:" + orderId);

			notificationService.sendPaymentSuccessNotification(orderMapper.toOrderResponse(order));

			logSuccess("PAYMENT_SUCCESS", orderId);
		} else {
			logFail("PAYMENT_STATUS_NOT_SUCCESS", status);
			throw new AppException(ErrorCode.INVALID_REQUEST_DATA, "Unsupported payment status: " + status);
		}
	}
}
