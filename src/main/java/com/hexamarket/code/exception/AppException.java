package com.hexamarket.code.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode;

	public AppException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public AppException(ErrorCode errorCode, String customMessage) {
		super(customMessage);
		this.errorCode = errorCode;
	}
}