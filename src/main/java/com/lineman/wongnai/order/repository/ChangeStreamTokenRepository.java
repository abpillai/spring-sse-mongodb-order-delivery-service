package com.lineman.wongnai.order.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.lineman.wongnai.order.domain.ChangeStreamToken;



public interface ChangeStreamTokenRepository extends MongoRepository<ChangeStreamToken, ObjectId>  {

	Optional<ChangeStreamToken> findByResumeToken(String token);
	
	Optional<ChangeStreamToken> findByService(String service);
}
