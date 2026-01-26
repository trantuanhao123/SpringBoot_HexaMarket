package com.hexamarket.code.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexamarket.code.dto.request.ProductSearchRequest;
import com.hexamarket.code.dto.response.ProductResponse;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.ProductMapper;
import com.hexamarket.code.repository.ProductRepository;
import com.hexamarket.code.repository.Specification.ProductSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	// Tìm kiếm nâng cao sử dụng JPA Specification
	@Transactional(readOnly = true)
	public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
		var spec = ProductSpecification.filterByRequest(request);
		// Trả về Page<ProductResponse> thay vì Entity trực tiếp
		return productRepository.findAll(spec, pageable).map(productMapper::toProductResponse);
	}

	// Soft delete sản phẩm
	@Transactional
	public void deleteProduct(Long id) {
		if (!productRepository.existsById(id)) {
			throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
		}
		productRepository.deleteById(id);
	}
}
