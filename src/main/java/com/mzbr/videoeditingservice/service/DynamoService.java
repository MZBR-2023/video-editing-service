package com.mzbr.videoeditingservice.service;

import java.util.Collections;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Service
@RequiredArgsConstructor
public class DynamoService {
	DynamoDbClient dynamoDbClient;

	public GetItemResponse getItemResponse(String tableName, String idName, Long id) {
		return dynamoDbClient.getItem(GetItemRequest.builder()
			.tableName(tableName)
			.key(Collections.singletonMap(idName, AttributeValue.builder().n(String.valueOf(id)).build()))
			.build());
	}

	public UpdateItemResponse updateStatus(String tableName, String idName, String statusName, Long id,
		String newStatus) {
		return dynamoDbClient.updateItem(UpdateItemRequest.builder()
			.tableName(tableName)
			.key(Collections.singletonMap(idName, AttributeValue.builder().n(String.valueOf(id)).build()))
			.updateExpression("SET #status = :newStatus")
			.expressionAttributeNames(Collections.singletonMap("#status", statusName))
			.expressionAttributeValues(
				Collections.singletonMap(":newStatus", AttributeValue.builder().s(newStatus).build()))
			.build());
	}
}
