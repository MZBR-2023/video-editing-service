package com.mzbr.videoeditingservice.config;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Value("${cloud.aws.credentials.access-key}")
	String AWS_ACCESS_KEY_ID;

	@Value("${cloud.aws.credentials.secret-key}")
	String AWS_SECRET_ACCESS_KEY;

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
			AWS_ACCESS_KEY_ID,
			AWS_SECRET_ACCESS_KEY);

		return S3Client.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)))
			.build();
	}

	@PreDestroy
	public void cleanup() {
		s3Presigner().close();
	}
}
