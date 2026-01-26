package com.hexamarket.code.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexamarket.code.dto.response.CategoryResponse;
import com.hexamarket.code.mapper.CategoryMapper;
import com.hexamarket.code.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService extends BaseService {
	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	@Transactional(readOnly = true)
	public List<CategoryResponse> getCategoryTree() {
		logStart("GET_CATEGORY_TREE");
		var roots = categoryRepository.findByParentIsNull();
		logSuccess("GET_CATEGORY_TREE", roots.size());
		return categoryMapper.toCategoryResponseList(roots);
	}

}
