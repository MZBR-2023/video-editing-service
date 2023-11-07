package com.mzbr.videoeditingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;

@Configuration
@Slf4j
public class KinesisConfig {
	@Value("${cloud.aws.kinesis.credentials.access-key}")
	private String AWS_ACCESS_KEY_ID;

	@Value("${cloud.aws.kinesis.credentials.secret-key}")
	private String AWS_SECRET_ACCESS_KEY;

	@Bean
	public KinesisClient kinesisClient() {
		return KinesisClient.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY_ID,AWS_SECRET_ACCESS_KEY)))
			.build();
	}

	@Bean
	public KinesisAsyncClient kinesisAsyncClient() {
		return KinesisAsyncClient.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY_ID,AWS_SECRET_ACCESS_KEY)))
			.build();
	}
}
