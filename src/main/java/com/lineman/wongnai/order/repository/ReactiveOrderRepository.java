package com.lineman.wongnai.order.repository;

import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.Order;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data MongoDB reactive repository for the Order entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReactiveOrderRepository extends ReactiveMongoRepository<Order, String> {
    Flux<Order> findAllBy(Pageable pageable);
    
    //@Tailable
	//Flux<Order> findWithTailableCursorByIdAndTimeAfter(String id,Instant from);
}
