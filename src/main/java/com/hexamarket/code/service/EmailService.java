package com.hexamarket.code.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
			throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
		}
	}
}
