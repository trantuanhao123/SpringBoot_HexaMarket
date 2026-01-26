package com.hexamarket.code.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hexamarket.code.dto.response.OrderResponse;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EmailService {
	private final JavaMailSender mailSender;

	@Async
	public void sendOtpEmail(String to, String otp) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(to);
			helper.setSubject("Hexa Market - Mã xác thực tài khoản");
			helper.setText("<html><body>" + "<h3>Xin chào!</h3>"
					+ "<p>Mã xác thực của bạn là: <b style='color:red; font-size:20px;'>" + otp + "</b></p>"
					+ "<p>Mã này có hiệu lực trong 5 phút.</p>" + "</body></html>", true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

	@Async
	public void sendOrderSuccessEmail(OrderResponse order) {

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(order.getEmail());
			helper.setSubject("Hexa Market - Order Confirmed #" + order.getOrderId());

			String content = """
					    <h2>🎉 Đơn hàng của bạn đã được xác nhận!</h2>
					    <p>Mã đơn: <b>%d</b></p>
					    <p>Tổng tiền: <b>%s VNĐ</b></p>
					    <p>Trạng thái: <b>%s</b></p>
					    <br/>
					    <p>Cảm ơn bạn đã mua sắm tại Hexa Market ❤️</p>
					""".formatted(order.getOrderId(), order.getTotalAmount(), order.getStatus());

			helper.setText(content, true);
			mailSender.send(message);

		} catch (Exception e) {
			throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

}
