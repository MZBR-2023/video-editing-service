package com.mzbr.videoeditingservice.model;

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
public class VideoEditingDynamoTable {
	private Long id;
	private Integer failureCount;
	private String status;

	@DynamoDbPartitionKey
	public Long getId() {
		return id;
	}

}
