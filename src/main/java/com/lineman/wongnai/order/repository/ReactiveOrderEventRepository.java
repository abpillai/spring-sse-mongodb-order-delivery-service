package com.lineman.wongnai.order.repository;

import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.Order;
import com.lineman.wongnai.order.domain.OrderEvent;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data MongoDB reactive repository for the Order entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReactiveOrderEventRepository extends ReactiveMongoRepository<OrderEvent, String> {
  
    
    //@Tailable
    //Flux<OrderEvent> findByTimeBetween(Instant from, Instant to);
	
	Mono<OrderEvent> findByOrderId(String id);
	
	@Tailable
	Flux<OrderEvent> findWithTailableCursorByOrderId(String id);
	
	//@Tailable
	//Flux<OrderEvent> findWithTailableCursorBy();
   
}
