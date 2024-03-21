package com.lineman.wongnai.order.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;

import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.OrderEvent;

import reactor.core.publisher.Flux;

/**
 * Spring Data MongoDB reactive repository for the Driver entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReactiveDriverRepository extends ReactiveMongoRepository<Driver, String> {
    Flux<Driver> findAllBy(Pageable pageable);
    
  
    
   
}
