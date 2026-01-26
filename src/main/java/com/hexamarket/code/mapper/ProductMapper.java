package com.hexamarket.code.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hexamarket.code.dto.response.ProductResponse;
import com.hexamarket.code.entity.Category;
import com.hexamarket.code.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	// ENTITY → RESPONSE
	@Mapping(target = "categoryName", source = "category", qualifiedByName = "mapCategoryName")
	ProductResponse toProductResponse(Product product);

	// UPDATE ENTITY (nếu có ProductUpdateRequest sau này)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "variants", ignore = true)
	@Mapping(target = "category", ignore = true)
	void updateProduct(@MappingTarget Product product, Object request); // đổi Object thành ProductUpdateRequest khi có

	// CUSTOM MAPPING
	@Named("mapCategoryName")
	default String mapCategoryName(Category category) {
		return category != null ? category.getName() : null;
	}
}
