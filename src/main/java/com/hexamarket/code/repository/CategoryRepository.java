package com.hexamarket.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hexamarket.code.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	@EntityGraph(attributePaths = { "children" })
	List<Category> findByParentIsNull();
}
