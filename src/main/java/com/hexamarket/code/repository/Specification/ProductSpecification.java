package com.hexamarket.code.repository.Specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hexamarket.code.dto.request.ProductSearchRequest;
import com.hexamarket.code.entity.Product;
import com.hexamarket.code.entity.ProductVariant;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class ProductSpecification {

	public static Specification<Product> filterByRequest(ProductSearchRequest request) {
		return (root, query, cb) -> {
			query.distinct(true);
			var predicate = cb.conjunction();

			// 1. Filter theo tên
			if (StringUtils.hasText(request.getName())) {
				predicate = cb.and(predicate,
						cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
			}

			// 2. Filter theo Category (Mới bổ sung)
			if (request.getCategoryId() != null) {
				predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), request.getCategoryId()));
			}

			// 3. Filter theo khoảng giá (Mới bổ sung)
			if (request.getMinPrice() != null) {
				predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
			}
			if (request.getMaxPrice() != null) {
				predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
			}

			// 4. Filter theo JSONB attributes
			if (StringUtils.hasText(request.getAttributeKey()) && StringUtils.hasText(request.getAttributeValue())) {
				Join<Product, ProductVariant> variants = root.join("variants", JoinType.INNER); // Dùng INNER JOIN khi
																								// bắt buộc phải có
																								// thuộc tính này

				Expression<String> jsonValue = cb.function("jsonb_extract_path_text", String.class,
						variants.get("attributes"), cb.literal(request.getAttributeKey()));

				predicate = cb.and(predicate,
						cb.equal(cb.lower(cb.coalesce(jsonValue, "")), request.getAttributeValue().toLowerCase()));
			}

			return predicate;
		};
	}
}
