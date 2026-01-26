package com.hexamarket.code.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemRequest {
	@NotNull(message = "INVALID_REQUEST_DATA")
	Long variantId;

	@NotNull(message = "INVALID_REQUEST_DATA")
	@Min(value = 1, message = "INVALID_QUANTITY")
	Integer quantity;
}
