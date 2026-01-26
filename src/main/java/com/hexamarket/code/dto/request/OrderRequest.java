package com.hexamarket.code.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	@NotBlank
	private String shippingAddress;

	@NotBlank
	@Size(max = 15)
	@Pattern(regexp = "^[0-9+\\- ]+$", message = "Invalid phone number")
	private String shippingPhone;

	private String note;

	@NotEmpty
	private List<CartItemRequest> items;
}
