package com.hexamarket.code.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hexamarket.code.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
	@Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.id = :id")
	Optional<ProductVariant> findByIdWithProduct(@Param("id") Long id);

	@Override
	@EntityGraph(attributePaths = { "product" })
	List<ProductVariant> findAllById(Iterable<Long> ids);
}
