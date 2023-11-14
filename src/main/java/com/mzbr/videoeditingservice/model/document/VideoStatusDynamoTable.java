package com.mzbr.videoeditingservice.model.document;

import java.util.List;

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
public class VideoStatusDynamoTable {
	private Long id;
	private Integer segmentCount;
	private List<Boolean> p144Status;
	private List<Boolean> p360Status;
	private List<Boolean> p480Status;
	private List<Boolean> p720Status;


	@DynamoDbPartitionKey
	public Long getId() {
		return id;
	}

}
