package com.lineman.wongnai.order.domain.enumeration;

/**
 * The OrderStatus enumeration.
 */
public enum OrderStatus {
    INIT(0),
    CANCELLED(0),
    ACCEPTED(5),
    ASSIGNED(5),
    PREPARED(30),
    DELIVERED(5),
	PICKEDUP(0);
	OrderStatus(int i) {
		// TODO Auto-generated constructor stub
	}
}
