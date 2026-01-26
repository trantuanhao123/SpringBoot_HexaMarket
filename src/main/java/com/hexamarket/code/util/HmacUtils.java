package com.hexamarket.code.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

public class HmacUtils {
	public static String hmacSHA512(String key, String data) {
		try {
			if (key == null || data == null)
				throw new NullPointerException();
			Mac hmac512 = Mac.getInstance("HmacSHA512");
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
			hmac512.init(secretKey);
			byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return Hex.encodeHexString(bytes);
		} catch (Exception e) {
			throw new RuntimeException("Error calculating HMAC", e);
		}
	}
}