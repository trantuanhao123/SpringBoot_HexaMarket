package com.hexamarket.code.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hexamarket.code.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
	@Override
	@EntityGraph(attributePaths = { "category" }) // Chỉ định các quan hệ muốn lấy luôn
	Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
