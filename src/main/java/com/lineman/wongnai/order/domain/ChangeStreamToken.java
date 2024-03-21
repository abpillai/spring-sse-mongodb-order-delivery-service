package com.lineman.wongnai.order.domain;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Document(collection = "cs_token")
public class ChangeStreamToken {

	@Id
	@Field("_id")
	private ObjectId id;
	
	@Field("token")
	private String resumeToken;
	
	@Field("service")
	private String service;
	
	@Field("create_time")
	private Instant createTime;
	
	@Field("update_time")
	private Instant updateTime;
	
	@Field("prev_token")
	private String previousToken;
	
	@Field("lastUpdatedCollection")
	private String lastUpdatedCollection;
	
	@Field("lastUpdatedAuditId")
	private String lastUpdatedAuditEventId;
}
