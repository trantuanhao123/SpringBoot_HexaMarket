package com.hexamarket.code.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hexamarket.code.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	@Query("""
			    SELECT o FROM Order o
			    LEFT JOIN FETCH o.items i
			    LEFT JOIN FETCH i.variant v
			    LEFT JOIN FETCH v.product
			    WHERE o.id = :id
			""")
	Optional<Order> findByIdWithItems(Long id);

}
