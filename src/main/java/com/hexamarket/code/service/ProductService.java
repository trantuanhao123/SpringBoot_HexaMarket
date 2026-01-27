package com.hexamarket.code.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexamarket.code.dto.request.ProductSearchRequest;
import com.hexamarket.code.dto.response.ProductResponse;
import com.hexamarket.code.entity.Product;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.ProductMapper;
import com.hexamarket.code.repository.ProductRepository;
import com.hexamarket.code.repository.Specification.ProductSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService extends BaseService {

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	/*
	 * ========================================================= SEARCH
	 * =========================================================
	 */
	@Transactional(readOnly = true)
	public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
		logStart("SEARCH_PRODUCTS", request);

		var spec = ProductSpecification.filterByRequest(request);
		Page<ProductResponse> result = productRepository.findAll(spec, pageable).map(productMapper::toProductResponse);

		logSuccess("SEARCH_PRODUCTS", "total=" + result.getTotalElements());
		return result;
	}

	/*
	 * ========================================================= GET DETAIL — CACHE
	 * =========================================================
	 */
	@Cacheable(value = "productDetail", key = "#id")
	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long id) {
		logStart("GET_PRODUCT_DETAIL", id);

		Product product = productRepository.findWithDetailsById(id).orElse(null);
		require(product != null, ErrorCode.PRODUCT_NOT_FOUND);

		ProductResponse response = productMapper.toProductResponse(product);

		logSuccess("GET_PRODUCT_DETAIL", id);
		return response;
	}

	/*
	 * ========================================================= CREATE
	 * =========================================================
	 */
	public ProductResponse createProduct(Product product) {
		logStart("CREATE_PRODUCT", product.getName());

		notNull(product, ErrorCode.INVALID_REQUEST);

		Product saved = productRepository.save(product);

		logSuccess("CREATE_PRODUCT", saved.getId());
		return productMapper.toProductResponse(saved);
	}

	/*
	 * ========================================================= UPDATE — EVICT
	 * CACHE =========================================================
	 */
	@CacheEvict(value = "productDetail", key = "#id")
	public ProductResponse updateProduct(Long id, Product updated) {
		logStart("UPDATE_PRODUCT", id);

		Product product = productRepository.findById(id).orElse(null);
		require(product != null, ErrorCode.PRODUCT_NOT_FOUND);

		product.setName(updated.getName());
		product.setDescription(updated.getDescription());
		product.setBasePrice(updated.getBasePrice());
		product.setCategory(updated.getCategory());

		Product saved = productRepository.save(product);

		logSuccess("UPDATE_PRODUCT", id);
		return productMapper.toProductResponse(saved);
	}

	/*
	 * ========================================================= DELETE — EVICT
	 * CACHE =========================================================
	 */
	@CacheEvict(value = "productDetail", key = "#id")
	public void deleteProduct(Long id) {
		logStart("DELETE_PRODUCT", id);

		require(productRepository.existsById(id), ErrorCode.PRODUCT_NOT_FOUND);
		productRepository.deleteById(id);

		logSuccess("DELETE_PRODUCT", id);
	}
}
