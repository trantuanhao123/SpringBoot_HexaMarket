package com.hexamarket.code;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class CodeApplication {

	public static void main(String[] args) {
		// Set cứng TimeZone của Java về chuẩn quốc tế "Asia/Ho_Chi_Minh"
		// Phù hợp với Postgre 15+.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		// NẠP BIẾN MÔI TRƯỜNG TỪ FILE .ENV
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		// KHỞI ĐỘNG SPRING BOOT
		SpringApplication.run(CodeApplication.class, args);
	}

}