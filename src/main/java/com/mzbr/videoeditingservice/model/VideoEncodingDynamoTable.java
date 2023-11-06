package com.mzbr.videoeditingservice.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
@Data
public class VideoEncodingDynamoTable {
	private Long id;
	private String status;
	private String format;
}
