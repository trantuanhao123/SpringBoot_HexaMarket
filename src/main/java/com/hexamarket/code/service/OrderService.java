package com.hexamarket.code.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.hexamarket.code.constant.OrderStatus;
import com.hexamarket.code.dto.request.CartItemRequest;
import com.hexamarket.code.dto.request.OrderRequest;
import com.hexamarket.code.dto.response.OrderResponse;
import com.hexamarket.code.entity.Inventory;
import com.hexamarket.code.entity.Order;
import com.hexamarket.code.entity.OrderItem;
import com.hexamarket.code.entity.ProductVariant;
import com.hexamarket.code.entity.User;
import com.hexamarket.code.exception.AppException;
import com.hexamarket.code.exception.ErrorCode;
import com.hexamarket.code.mapper.OrderMapper;
import com.hexamarket.code.repository.OrderRepository;
import com.hexamarket.code.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService extends BaseService {

	private final OrderRepository orderRepository;
	private final InventoryService inventoryService;
	private final CartService cartService;
	private final UserRepository userRepository;
	private final OrderMapper orderMapper;
	private final StringRedisTemplate redisTemplate;

	@Transactional
	public OrderResponse checkout(Long userId, OrderRequest request) {

		logStart("CHECKOUT", userId);

		// VALIDATION
		notNull(request, ErrorCode.INVALID_REQUEST);
		require(request.getItems() != null && !request.getItems().isEmpty(), ErrorCode.CART_EMPTY);

		// Merge duplicate variantId nếu client gửi trùng
		Map<Long, Integer> itemsMap = request.getItems().stream()
				.peek(i -> require(i.getQuantity() > 0, ErrorCode.INVALID_QUANTITY))
				.collect(Collectors.toMap(CartItemRequest::getVariantId, CartItemRequest::getQuantity, Integer::sum));

		// LOCK & DEDUCT STOCK
		List<Inventory> inventories = inventoryService.deductStocks(itemsMap);

		require(!inventories.isEmpty(), ErrorCode.PRODUCT_NOT_FOUND);

		BigDecimal totalAmount = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		for (Inventory inv : inventories) {

			ProductVariant variant = inv.getVariant();
			Integer quantity = itemsMap.get(variant.getId());

			notNull(variant, ErrorCode.PRODUCT_NOT_FOUND);
			notNull(variant.getPrice(), ErrorCode.PRODUCT_PRICE_INVALID);

			BigDecimal subTotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
			totalAmount = totalAmount.add(subTotal);

			orderItems.add(OrderItem.builder().variant(variant).productName(variant.getProduct().getName())
					.quantity(quantity).price(variant.getPrice()) // snapshot price
					.build());
		}

		// CREATE ORDER
		User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

		Order order = Order.builder().user(user).status(OrderStatus.PENDING).paymentStatus("UNPAID")
				.totalAmount(totalAmount).shippingAddress(request.getShippingAddress())
				.shippingPhone(request.getShippingPhone()).note(request.getNote()).items(orderItems).build();

		orderItems.forEach(i -> i.setOrder(order));

		Order savedOrder = orderRepository.save(order);

		// AFTER COMMIT
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				cartService.clearCart(userId);
			}
		});
		// Set Redis TTL 30 minutes
		String redisKey = "order:timeout:" + savedOrder.getId();
		redisTemplate.opsForValue().set(redisKey, "PENDING", 30, TimeUnit.MINUTES);
		logSuccess("CHECKOUT", savedOrder.getId(), totalAmount);
		return orderMapper.toOrderResponse(savedOrder);
	}

	@Transactional
	public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
		// 1. Tìm Order
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

		// 2. Validate bằng hàm bạn đã viết trong Enum
		if (!order.getStatus().canTransitionTo(newStatus)) {
			throw new AppException(ErrorCode.INVALID_STATE_TRANSITION,
					String.format("Không được phép chuyển từ %s sang %s", order.getStatus(), newStatus));
		}

		// 3. Cập nhật và lưu
		order.setStatus(newStatus);
		Order savedOrder = orderRepository.save(order);

		return orderMapper.toOrderResponse(savedOrder);
	}

	// Xử lý hủy đơn (cho Listener và cả API hủy thủ công)
	@Transactional
	public void cancelUnpaidOrder(Long orderId) {

		logStart("AUTO_CANCEL_ORDER", orderId);

		notNull(orderId, ErrorCode.INVALID_REQUEST_DATA);

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

		// Nếu đã PAID mà listener vẫn chạy → BUG hệ thống
		require("UNPAID".equals(order.getPaymentStatus()), ErrorCode.INVALID_STATE_TRANSITION);

		// Validate state machine
		require(order.getStatus().canTransitionTo(OrderStatus.CANCELLED), ErrorCode.INVALID_STATE_TRANSITION);

		log.info("Cancelling unpaid order: {}", orderId);

		/* ===== 1. UPDATE STATUS ===== */
		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);

		/* ===== 2. RESTORE STOCK ===== */
		Map<Long, Integer> itemsToRestore = order.getItems().stream()
				.collect(Collectors.toMap(item -> item.getVariant().getId(), OrderItem::getQuantity));

		inventoryService.restoreStocks(itemsToRestore);

		/* ===== 3. DELETE REDIS KEY ===== */
		redisTemplate.delete("order:timeout:" + orderId);

		logSuccess("AUTO_CANCEL_DONE", orderId);
	}

}
