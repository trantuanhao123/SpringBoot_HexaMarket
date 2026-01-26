package com.hexamarket.code.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String EXCHANGE = "hexamarket.exchange";
	public static final String EMAIL_QUEUE = "hexamarket.email.queue";
	public static final String ROUTING_KEY = "email.payment.success";

	/* ===== EXCHANGE ===== */
	@Bean
	public DirectExchange exchange() {
		return new DirectExchange(EXCHANGE, true, false);
	}

	/* ===== QUEUE (DURABLE) ===== */
	@Bean
	public Queue emailQueue() {
		return QueueBuilder.durable(EMAIL_QUEUE).withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
				.withArgument("x-dead-letter-routing-key", ROUTING_KEY + ".dlq").build();
	}

	/* ===== BINDING ===== */
	@Bean
	public Binding binding() {
		return BindingBuilder.bind(emailQueue()).to(exchange()).with(ROUTING_KEY);
	}

	/* ===== JSON CONVERTER ===== */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
