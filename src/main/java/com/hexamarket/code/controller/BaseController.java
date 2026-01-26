package com.hexamarket.code.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hexamarket.code.dto.response.ApiResponse;

public abstract class BaseController {

	protected <T> ResponseEntity<ApiResponse<T>> ok(T data) {
		return ResponseEntity.ok(ApiResponse.<T>builder().result(data).build());
	}

	protected ResponseEntity<ApiResponse<Void>> okMessage(String message) {
		return ResponseEntity.ok(ApiResponse.<Void>builder().message(message).build());
	}

	protected <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<T>builder().message(message).result(data).build());
	}

	protected ResponseEntity<ApiResponse<Void>> deleted(String message) {
		return ResponseEntity.ok(ApiResponse.<Void>builder().message(message).build());
	}
}
