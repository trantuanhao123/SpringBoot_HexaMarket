package com.hexamarket.code.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.hexamarket.code.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j; // Dùng để log lỗi ra console

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// 1. Bắt tất cả các lỗi không xác định (Fallback)
	@ExceptionHandler(value = Exception.class)
	ResponseEntity<ApiResponse> handlingException(Exception exception) {

		// Log full stack trace để debug
		log.error("Uncategorized Exception: ", exception);

		ApiResponse apiResponse = new ApiResponse();

		apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
		// Có thể cộng thêm exception.getMessage() để biết lỗi gì, hoặc chỉ để message
		// chung
		apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage() + ": " + exception.getMessage());

		return ResponseEntity.badRequest().body(apiResponse);
	}

	// 2. Bắt lỗi do chính mình định nghĩa (AppException)
	@ExceptionHandler(value = AppException.class)
	ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		ApiResponse apiResponse = new ApiResponse();

		apiResponse.setCode(errorCode.getCode());
		apiResponse.setMessage(errorCode.getMessage());

		return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
	}

	// 3. Bắt lỗi Validation (VD: @Size, @NotNull trong DTO)
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
		String enumKey = exception.getFieldError().getDefaultMessage();

		ErrorCode errorCode = ErrorCode.INVALID_KEY;
		try {
			errorCode = ErrorCode.valueOf(enumKey);
		} catch (IllegalArgumentException e) {
			// Nếu message trong DTO không khớp với Enum thì dùng lỗi mặc định
		}

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setCode(errorCode.getCode());
		apiResponse.setMessage(errorCode.getMessage());

		return ResponseEntity.badRequest().body(apiResponse);
	}
}