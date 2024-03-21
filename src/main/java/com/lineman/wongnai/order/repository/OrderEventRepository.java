package com.lineman.wongnai.order.repository;


import org.springframework.data.mongodb.repository.MongoRepository;

import com.lineman.wongnai.order.domain.OrderEvent;

public interface OrderEventRepository extends MongoRepository<OrderEvent, String> {

}
