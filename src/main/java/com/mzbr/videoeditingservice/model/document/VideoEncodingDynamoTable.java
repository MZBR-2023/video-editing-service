package com.mzbr.videoeditingservice.model.document;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VideoEncodingDynamoTable {
	private String id;
	private Long rdbId;
	private String status;
	private String format;
	private Integer failureCount;

	@DynamoDbPartitionKey
	public String getId() {
		return id;
	}
}
