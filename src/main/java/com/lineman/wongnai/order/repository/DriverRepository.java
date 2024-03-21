package com.lineman.wongnai.order.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.lineman.wongnai.order.domain.Driver;

public interface DriverRepository extends MongoRepository<Driver, String> {

}
