package com.hexamarket.code.component;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderTimeoutListener extends KeyExpirationEventMessageListener {

	private final OrderService orderService;

	public OrderTimeoutListener(RedisMessageListenerContainer listenerContainer, OrderService orderService) {
		super(listenerContainer);
		this.orderService = orderService;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String expiredKey = message.toString();

		if (!expiredKey.startsWith("order:timeout:")) {
			return;
		}

		try {
			Long orderId = Long.parseLong(expiredKey.split(":")[2]);
			log.info("[REDIS-EXPIRE] Order timeout triggered for orderId={}", orderId);

			orderService.cancelUnpaidOrder(orderId); // service sẽ throw AppException nếu sai

		} catch (NumberFormatException e) {
			log.error("[REDIS-EXPIRE] Invalid orderId format in key={}", expiredKey, e);
		} catch (AppException e) {
			// Lỗi nghiệp vụ — KHÔNG throw ra ngoài (tránh crash listener)
			log.error("[REDIS-EXPIRE][BUSINESS-ERROR] code={}, msg={}", e.getErrorCode().getCode(), e.getMessage());
		} catch (Exception e) {
			log.error("[REDIS-EXPIRE][SYSTEM-ERROR] Unexpected error", e);
		}
	}
}
