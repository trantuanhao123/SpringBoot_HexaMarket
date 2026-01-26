package com.hexamarket.code.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hexamarket.code.dto.response.ProductVariantResponse;
import com.hexamarket.code.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

	// ENTITY → RESPONSE
	@Mapping(target = "productId", source = "product.id")
	@Mapping(target = "imageUrl", source = "imageUrl")
	ProductVariantResponse toProductVariantResponse(ProductVariant variant);

	// UPDATE ENTITY (khi có Update DTO)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "product", ignore = true) // Không cho đổi FK product
	void updateVariant(@MappingTarget ProductVariant variant, Object request);
}
