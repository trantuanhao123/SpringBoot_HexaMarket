package com.hexamarket.code.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.hexamarket.code.config.RabbitMQConfig;
import com.hexamarket.code.dto.response.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService extends BaseService {

	private final RabbitTemplate rabbitTemplate;
	private final EmailService emailService;

	/* ================= PRODUCER ================= */
	public void sendPaymentSuccessNotification(OrderResponse order) {

		logStart("PUSH_EMAIL_EVENT", order.getOrderId());

		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, order);

		logSuccess("EVENT_PUSHED_TO_QUEUE", order.getOrderId());
	}

	/* ================= CONSUMER ================= */
	@RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
	public void handlePaymentSuccessEvent(OrderResponse order) {

		logStart("CONSUME_EMAIL_EVENT", order.getOrderId());

		try {
			emailService.sendOrderSuccessEmail(order);
			logSuccess("EMAIL_SENT", order.getOrderId());

		} catch (Exception e) {
			logFail("EMAIL_FAIL_RETRY", e.getMessage());
			throw e;
		}
	}
}
