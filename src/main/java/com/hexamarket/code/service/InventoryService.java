package com.hexamarket.code.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexamarket.code.entity.Inventory;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService extends BaseService {
	private final InventoryRepository inventoryRepository;

	@Transactional
	public List<Inventory> deductStocks(Map<Long, Integer> itemsToDeduct) {
		logStart("DEDUCT_STOCK", itemsToDeduct);

		require(itemsToDeduct != null && !itemsToDeduct.isEmpty(), ErrorCode.INVALID_REQUEST_DATA);

		List<Long> sortedIds = itemsToDeduct.keySet().stream().sorted().toList();
		List<Inventory> inventories = inventoryRepository.findAllByVariantIdsWithLock(sortedIds);

		require(inventories.size() == itemsToDeduct.size(), ErrorCode.INVENTORY_NOT_FOUND);

		for (Inventory inv : inventories) {
			Integer qty = itemsToDeduct.get(inv.getVariant().getId());
			require(qty != null && qty > 0, ErrorCode.INVALID_REQUEST_DATA);

			int available = inv.getQuantity() - inv.getReservedQuantity();
			require(available >= qty, ErrorCode.OUT_OF_STOCK);

			inv.setQuantity(inv.getQuantity() - qty);
		}

		logSuccess("DEDUCT_STOCK");
		return inventories;
	}

}
