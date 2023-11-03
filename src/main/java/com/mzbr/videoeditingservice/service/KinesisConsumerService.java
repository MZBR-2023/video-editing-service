package com.mzbr.videoeditingservice.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

@Service
@RequiredArgsConstructor
public class KinesisConsumerService {
	private final KinesisClient kinesisClient;
	private final KinesisAsyncClient kinesisAsyncClient;

	@Value("${cloud.aws.kinesis.name}")
	private String STREAM_NAME;

	@PostConstruct
	public void init() {
		System.out.println(STREAM_NAME);
		String shardId = kinesisAsyncClient.listShards(ListShardsRequest.builder()
				.streamName(STREAM_NAME)
				.build())
			.join() // 비동기 호출을 동기식으로 기다립니다
			.shards()
			.get(0)
			.shardId();

		// 샤드의 ShardIterator를 가져옵니다
		String shardIterator = kinesisAsyncClient.getShardIterator(GetShardIteratorRequest.builder()
				.streamName(STREAM_NAME)
				.shardId(shardId)
				.shardIteratorType(ShardIteratorType.LATEST)
				.build())
			.join()
			.shardIterator();
		pollShard(shardIterator);
	}

	private void pollShard(String shardIterator) {
		CompletableFuture<GetRecordsResponse> getRecordsFuture = kinesisAsyncClient.getRecords(GetRecordsRequest.builder()
			.shardIterator(shardIterator)
			.limit(1000)
			.build());

		getRecordsFuture.thenAcceptAsync(getRecordsResponse -> {
			getRecordsResponse.records().forEach(record -> {
				String data = StandardCharsets.UTF_8.decode(record.data().asByteBuffer()).toString();
				
			});

			String nextShardIterator = getRecordsResponse.nextShardIterator();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

			pollShard(nextShardIterator);
		});
	}
}
