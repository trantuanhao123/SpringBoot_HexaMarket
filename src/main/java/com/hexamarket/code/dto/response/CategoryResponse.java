package com.hexamarket.code.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {
	private Long id;
	private String name;
	private String slug;
	// Cấu trúc cây đệ quy
	private List<CategoryResponse> children;
}