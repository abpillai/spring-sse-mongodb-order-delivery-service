package com.lineman.wongnai.order.stream.listener;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.lineman.wongnai.order.domain.ChangeStreamToken;
import com.lineman.wongnai.order.domain.DriverEvent;
import com.lineman.wongnai.order.domain.OrderEvent;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;
import com.lineman.wongnai.order.repository.ChangeStreamTokenRepository;
import com.lineman.wongnai.order.repository.DriverEventRepository;
import com.lineman.wongnai.order.repository.OrderEventRepository;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChangeStreamListener implements CommandLineRunner {

	@Value("${spring.data.mongodb.uri}")
	String databaseUri;

	@Autowired
	private ChangeStreamTokenRepository changeStreamTokenRepository;

	@Autowired
	private OrderEventRepository orderEventRepository;

	@Autowired
	private DriverEventRepository driverEventRepository;

	@Override
	public void run(String... args) throws Exception {
		try {

			CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
			CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
					pojoCodecRegistry);

			ConnectionString connectionString = new ConnectionString(databaseUri);
			MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.codecRegistry(codecRegistry).build();
			try (MongoClient mongoClient = MongoClients.create(clientSettings)) {
				Optional<ChangeStreamToken> token = changeStreamTokenRepository.findByService("daleel");
				log.info("token present:" + token.isPresent());

				List<Bson> pipeline = Arrays
						.asList(Aggregates.match(Filters.in("operationType", Arrays.asList("insert"))));

				if (token.isPresent()) {

					BsonDocument resumeToken = new BsonDocument("_data", new BsonString(token.get().getResumeToken()));
					log.info("cs_token:" + resumeToken.getString("_data").getValue());
					mongoClient.watch(pipeline).resumeAfter(resumeToken).fullDocument(FullDocument.UPDATE_LOOKUP)
							.forEach(e -> handleEvent(e, token));

				} else {

					mongoClient.watch(pipeline).fullDocument(FullDocument.UPDATE_LOOKUP).forEach(e -> {
						log.info("cs_token:" + e.getResumeToken().getString("_data").getValue());
						handleEvent(e, Optional.empty());
					});

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void handleEvent(ChangeStreamDocument<Document> doc, Optional<ChangeStreamToken> token) {
		try {

			Optional<ChangeStreamToken> checkToken = changeStreamTokenRepository.findByService("daleel");
			if (checkToken.isPresent()) {
				String tokenVal = doc.getResumeToken().getString("_data").getValue();
				if (!checkToken.get().getResumeToken().equals(tokenVal))
					updateToken(doc, checkToken);
			} else {
				log.info("creating token:");
				createToken(doc);
			}

			handleChangeStreamDocument(doc);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private void createToken(ChangeStreamDocument<Document> doc) {
		log.info("creating token ###########");
		log.info("createToken docfull ###########" + doc.getFullDocument());
		try {
			ChangeStreamToken newToken = new ChangeStreamToken();
			newToken.setService("daleel");
			newToken.setResumeToken(doc.getResumeToken().getString("_data").getValue());
			newToken.setCreateTime(Instant.now());
			newToken.setLastUpdatedCollection(doc.getNamespace().getCollectionName());
			newToken.setLastUpdatedAuditEventId(doc.getFullDocument().get("_id").toString());
			changeStreamTokenRepository.save(newToken);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private void updateToken(ChangeStreamDocument<Document> doc, Optional<ChangeStreamToken> token) {
		log.info("updating token ###########");
		log.info("updateToken docfull ###########" + doc.getFullDocument());
		try {
			ChangeStreamToken existingToken = new ChangeStreamToken();
			existingToken.setId(token.get().getId());
			existingToken.setService("daleel");
			existingToken.setResumeToken(doc.getResumeToken().getString("_data").getValue());
			existingToken.setPreviousToken(token.get().getResumeToken());
			existingToken.setUpdateTime(Instant.now());
			existingToken.setLastUpdatedCollection(doc.getNamespace().getCollectionName());
			existingToken.setLastUpdatedAuditEventId(doc.getFullDocument().get("_id").toString());
			changeStreamTokenRepository.save(existingToken);

		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void handleChangeStreamDocument(ChangeStreamDocument<Document> doc) {
		try {
			log.info("updateToken docfull ###########" + doc.getFullDocument());
			String collection = doc.getNamespace().getCollectionName();
			switch (collection) {

			case "order":
				handleOrderEvent(doc.getFullDocument());
				break;

			case "driver":
				handleDriverEvent(doc.getFullDocument());
				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private void handleOrderEvent(Document doc) {
		try {

			OrderEvent event = new OrderEvent();
			event.setOrderId(doc.getObjectId("_id").toString());
			event.setStatus(OrderStatus.valueOf(doc.get("status").toString()));
			event.setTime(doc.getDate("time").toInstant());
			orderEventRepository.save(event);

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private void handleDriverEvent(Document doc) {
		try {

			DriverEvent event = new DriverEvent();
			event.setDriverId(doc.getObjectId("_id").toString());
			event.setStatus(doc.get("status").toString());
			event.setLatitude(doc.get("latitude").toString());
			event.setLongitude(doc.get("longitude").toString());
			driverEventRepository.save(event);

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

}
