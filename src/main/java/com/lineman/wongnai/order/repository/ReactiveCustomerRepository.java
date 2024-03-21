package com.lineman.wongnai.order.repository;

import com.lineman.wongnai.order.domain.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data MongoDB reactive repository for the Customer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReactiveCustomerRepository extends ReactiveMongoRepository<Customer, String> {
    Flux<Customer> findAllBy(Pageable pageable);
}
