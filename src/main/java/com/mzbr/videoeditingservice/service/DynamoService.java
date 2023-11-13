package com.mzbr.videoeditingservice.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mzbr.videoeditingservice.model.document.VideoEditingDynamoTable;
import com.mzbr.videoeditingservice.model.document.VideoEncodingDynamoTable;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Service
@RequiredArgsConstructor
public class DynamoService {
	private final DynamoDbClient dynamoDbClient;
	private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

	@Value("${cloud.dynamo.encoding-table}")
	private String ENCODING_TABLE_NAME;

	@Value("${cloud.dynamo.editing-table}")
	private String EDITING_TABLE_NAME;
	private final TableSchema<VideoEncodingDynamoTable> videoEncodingtableSchema = TableSchema.fromBean(
		VideoEncodingDynamoTable.class);

	private final TableSchema<VideoEditingDynamoTable> videoEditingDynamoTableTableSchema = TableSchema.fromBean(
		VideoEditingDynamoTable.class);

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

	public void videoEncodingListBatchSave(List<VideoEncodingDynamoTable> videoEncodingDynamoTableList) {

		DynamoDbTable<VideoEncodingDynamoTable> table = dynamoDbEnhancedClient.table(ENCODING_TABLE_NAME,
			videoEncodingtableSchema);
		TransactWriteItemsEnhancedRequest.Builder transactWriteItemsEnhancedRequest = TransactWriteItemsEnhancedRequest.builder();

		for (VideoEncodingDynamoTable videoEncodingDynamoTable : videoEncodingDynamoTableList) {
			transactWriteItemsEnhancedRequest.addPutItem(table, videoEncodingDynamoTable);
		}

		dynamoDbEnhancedClient.transactWriteItems(transactWriteItemsEnhancedRequest.build());
	}

	public void createVideoEditingNewDocument(Long id) {
		VideoEditingDynamoTable videoEditingDynamoTable = VideoEditingDynamoTable.builder()
			.id(id)
			.status("waiting")
			.failureCount(0)
			.build();
		DynamoDbTable<VideoEditingDynamoTable> table = dynamoDbEnhancedClient.table(EDITING_TABLE_NAME,
			videoEditingDynamoTableTableSchema);

		dynamoDbEnhancedClient.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
			.addPutItem(table, videoEditingDynamoTable)
			.build());
	}
}
