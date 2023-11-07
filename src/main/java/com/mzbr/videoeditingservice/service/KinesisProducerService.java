package com.mzbr.videoeditingservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

@Service
@RequiredArgsConstructor
public class KinesisProducerService {
	@Value("${cloud.aws.kinesis.producer-name}")
	private String STREAM_NAME;

	private final KinesisClient kinesisClient;

	public void publishUuidListToKinesis(List<String> uuidList) {
		List<PutRecordsRequestEntry> recordsList = new ArrayList<>();

		uuidList.forEach(uuid -> {
			recordsList.add(PutRecordsRequestEntry.builder()
				.partitionKey("video")
				.data(SdkBytes.fromUtf8String(uuid))
				.build());
		});

		PutRecordsRequest putRecordsRequest = PutRecordsRequest.builder()
			.streamName(STREAM_NAME)
			.records(recordsList)
			.build();
		PutRecordsResponse putRecordsResponse = kinesisClient.putRecords(putRecordsRequest);
	}
}
