package com.hexamarket.code.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;

public abstract class BaseService {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	/* ================= VALIDATION ================= */

	protected void require(boolean condition, ErrorCode error) {
		if (!condition)
			throw new AppException(error);
	}

	protected void notNull(Object obj, ErrorCode error) {
		if (obj == null)
			throw new AppException(error);
	}

	protected void notBlank(String str, ErrorCode error) {
		if (str == null || str.trim().isEmpty())
			throw new AppException(error);
	}

	/* ================= LOGGING ================= */

	protected void logStart(String action, Object... data) {
		log.info("[START] {} - {}", action, data);
	}

	protected void logSuccess(String action, Object... data) {
		log.info("[SUCCESS] {} - {}", action, data);
	}

	protected void logFail(String action, Object... data) {
		log.error("[FAIL] {} - {}", action, data);
	}
}
