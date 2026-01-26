package com.hexamarket.code.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.hexamarket.code.dto.response.CategoryResponse;
import com.hexamarket.code.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
	// MapStruct tự động xử lý đệ quy cho List<Category> children nếu các field
	// trùng tên
	CategoryResponse toCategoryResponse(Category category);

	List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}
