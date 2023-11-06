package com.mzbr.videoeditingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoConfig {
	@Value("${cloud.dynamo.credentials.access-key}")
	private String AWS_ACCESS_KEY_ID;

	@Value("${cloud.dynamo.credentials.secret-key}")
	private String AWS_SECRET_ACCESS_KEY;

	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder()
			.region(Region.AP_NORTHEAST_2) // 원하는 리전으로 교체하세요
			.credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)))
			.build();
	}

	@Bean
	public DynamoDbAsyncClient dynamoDbAsyncClient(){
		return DynamoDbAsyncClient.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)))
			.build();
	}

	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(){
		return DynamoDbEnhancedClient.builder()
			.dynamoDbClient(dynamoDbClient())
			.build();
	}

}
