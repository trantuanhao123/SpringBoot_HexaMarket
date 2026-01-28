package com.hexamarket.code.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexamarket.code.dto.response.ApiResponse;
import com.hexamarket.code.dto.response.CategoryResponse;
import com.hexamarket.code.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController extends BaseController {

	private final CategoryService categoryService;

	@PreAuthorize("permitAll()")
	@GetMapping("/tree")
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
		return ok(categoryService.getCategoryTree());
	}
}
