package com.hexamarket.code.constant;

public enum OrderStatus {
	PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED;

	public boolean canTransitionTo(OrderStatus next) {
		return switch (this) {
		case PENDING -> next == CONFIRMED || next == CANCELLED;
		case CONFIRMED -> next == SHIPPING || next == CANCELLED;
		case SHIPPING -> next == COMPLETED;
		default -> false; // COMPLETED và CANCELLED là trạng thái cuối
		};
	}
}
