package com.hexamarket.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.hexamarket.code.entity.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	// Lock bản ghi này lại cho đến khi Transaction kết thúc
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT i FROM Inventory i " + "JOIN FETCH i.variant v " + "JOIN FETCH v.product p "
			+ "WHERE v.id IN :variantIds " + "ORDER BY v.id ASC")
	List<Inventory> findAllByVariantIdsWithLock(List<Long> variantIds);
}
