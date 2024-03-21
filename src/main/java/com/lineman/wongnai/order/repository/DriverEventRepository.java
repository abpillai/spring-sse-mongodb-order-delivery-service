package com.lineman.wongnai.order.repository;


import org.springframework.data.mongodb.repository.MongoRepository;

import com.lineman.wongnai.order.domain.DriverEvent;
import com.lineman.wongnai.order.domain.OrderEvent;

public interface DriverEventRepository extends MongoRepository<DriverEvent, String> {

}
