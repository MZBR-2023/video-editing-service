package com.mzbr.videoeditingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"ssafy","prod"})
public class EditingKinesisProduceService {
	private final KinesisClient kinesisClient;
	@Value("${cloud.aws.kinesis.consumer-name}")
	private String STREAM_NAME;
	public PutRecordsResponse publishIdToKinesis(Long id) {
		PutRecordsRequestEntry putRecordsRequestEntry= PutRecordsRequestEntry.builder()
			.partitionKey("video")
			.data(SdkBytes.fromUtf8String(String.valueOf(id)))
			.build();
		PutRecordsRequest putRecordsRequest = PutRecordsRequest.builder()
			.streamName(STREAM_NAME)
			.records(putRecordsRequestEntry)
			.build();
		return kinesisClient.putRecords(putRecordsRequest);
	}
}
