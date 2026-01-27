package com.hexamarket.code.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.request.ProductSearchRequest;
import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.dto.response.PageResponse;
import com.hexamarket.code.dto.response.ProductResponse;
import com.hexamarket.code.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController extends BaseController {

	private final ProductService productService;

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(ProductSearchRequest request,
			Pageable pageable) {

		Page<ProductResponse> page = productService.searchProducts(request, pageable);
		return ok(new PageResponse<>(page));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
		return ok(productService.getProductById(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return deleted("Product deleted successfully");
	}
}
