package com.hexamarket.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {

	// ===== SYSTEM =====
	UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

	// ===== VALIDATION =====
	INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),
	INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),
	INVALID_REQUEST_DATA(1003, "Invalid request data", HttpStatus.BAD_REQUEST),
	INVALID_QUANTITY(1004, "Invalid quantity", HttpStatus.BAD_REQUEST),

	// ===== USER =====
	USER_EXISTED(1101, "User already existed", HttpStatus.BAD_REQUEST),
	USER_NOT_EXISTED(1102, "User not existed", HttpStatus.NOT_FOUND),
	USERNAME_INVALID(1103, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
	PASSWORD_INVALID(1104, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
	ACCOUNT_NOT_ACTIVE(1105, "Account not activated", HttpStatus.BAD_REQUEST),
	ACCOUNT_ALREADY_ACTIVE(1106, "Account already activated", HttpStatus.BAD_REQUEST),

	// ===== AUTH =====
	UNAUTHENTICATED(1201, "Unauthenticated", HttpStatus.UNAUTHORIZED),
	UNAUTHORIZED(1202, "You do not have permission", HttpStatus.FORBIDDEN),
	REFRESH_TOKEN_EXPIRED(1203, "Refresh token expired or revoked", HttpStatus.UNAUTHORIZED),
	INVALID_REQUEST_TOKEN(1204, "Invalid request token", HttpStatus.BAD_REQUEST),

	// ===== EMAIL / OTP =====
	EMAIL_EXISTED(1301, "Email already existed", HttpStatus.BAD_REQUEST),
	INVALID_OTP(1302, "Invalid OTP or expired", HttpStatus.BAD_REQUEST),
	EMAIL_SEND_FAILED(1303, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),

	// ===== ROLE =====
	ROLE_NOT_FOUND(1401, "Role not found", HttpStatus.INTERNAL_SERVER_ERROR),

	// ===== PRODUCT / ORDER =====
	PRODUCT_NOT_FOUND(1501, "Product not found", HttpStatus.NOT_FOUND),
	PRODUCT_PRICE_INVALID(1502, "Product price invalid", HttpStatus.INTERNAL_SERVER_ERROR),
	CART_EMPTY(1503, "Cart is empty", HttpStatus.BAD_REQUEST),

	// --- ADDED NEW CODES HERE ---
	ORDER_NOT_FOUND(1504, "Order not found", HttpStatus.NOT_FOUND),
	INVALID_STATE_TRANSITION(1505, "Invalid order status transition", HttpStatus.BAD_REQUEST),

	// ===== INVENTORY =====
	INVENTORY_NOT_FOUND(1601, "Inventory record not found", HttpStatus.NOT_FOUND),
	OUT_OF_STOCK(1602, "Insufficient stock", HttpStatus.BAD_REQUEST);

	ErrorCode(int code, String message, HttpStatusCode statusCode) {
		this.code = code;
		this.message = message;
		this.statusCode = statusCode;
	}

	private final int code;
	private final String message;
	private final HttpStatusCode statusCode;
}