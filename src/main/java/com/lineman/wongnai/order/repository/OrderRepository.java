package com.lineman.wongnai.order.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;

import com.lineman.wongnai.order.domain.Order;
import com.lineman.wongnai.order.domain.OrderEvent;

import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

	//findWithTailableCursorByIdAndTimeAfter
	
	
	
}
