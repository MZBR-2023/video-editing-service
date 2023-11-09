package com.mzbr.videoeditingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

@Service
@RequiredArgsConstructor
@Slf4j
public class KinesisConsumerService {
	private final KinesisAsyncClient kinesisAsyncClient;
	private final DynamoService dynamoService;
	private final VideoEditingServiceImpl videoEditingService;

	@Value("${cloud.aws.kinesis.consumer-name}")
	private String STREAM_NAME;

	private static final String JOB_TABLE = "video-editing-table";
	private static final String JOB_ID = "id";
	private static final String STATUS = "status";

	private static final String WAITING_STATUS = "waiting";
	private static final String IN_PROGRESS_STATUS = "in_progress";
	private static final String COMPLETED_STATUS = "completed";
	private static final String FAILED_STATUS = "failed";
	private static final int WIDTH = 720;
	private static final int HEIGHT = 1280;
	private static final String FOLDER_PATH = "editing-videos";

	@PostConstruct
	public void init() {
		String shardId = kinesisAsyncClient.listShards(ListShardsRequest.builder()
				.streamName(STREAM_NAME)
				.build())
			.join()
			.shards()
			.get(0)
			.shardId();

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
		CompletableFuture<GetRecordsResponse> getRecordsFuture = kinesisAsyncClient.getRecords(
			GetRecordsRequest.builder()
				.shardIterator(shardIterator)
				.limit(1000)
				.build());

		getRecordsFuture.thenAcceptAsync(getRecordsResponse -> {
			getRecordsResponse.records().forEach(record -> {
				String data = StandardCharsets.UTF_8.decode(record.data().asByteBuffer()).toString();
				updateAndProcessJob(data);
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

	@Async
	public CompletableFuture<Void> updateAndProcessJob(String idString) {
		Long id = Long.parseLong(idString);
		return CompletableFuture.supplyAsync(() -> {
			GetItemResponse getItemResponse = dynamoService.getItemResponse(JOB_TABLE, JOB_ID, id);
			if (getItemResponse.item() == null || getItemResponse.item().isEmpty()) {
				return null;
			}
			AttributeValue statusValue = getItemResponse.item().get(STATUS);
			if (statusValue == null || !WAITING_STATUS.equals(statusValue.s())) {
				return null;
			}
			updateJobStatus(id, IN_PROGRESS_STATUS);
			return id;
		}).thenCompose(result -> {
			if (result == null) {
				return CompletableFuture.completedFuture(null);
			}
			return processJob(id);
		}).thenRun(() -> {
			updateJobStatus(id, COMPLETED_STATUS);
		}).exceptionally(e -> {
			updateJobStatus(id, FAILED_STATUS);
			return null;
		});

	}

	private UpdateItemResponse updateJobStatus(long id, String newStatus) {
		return dynamoService.updateStatus(JOB_TABLE, JOB_ID, STATUS, id, newStatus);
	}

	private CompletableFuture<Void> processJob(long id) {
		return CompletableFuture.runAsync(() -> {
			try {
				videoEditingService.processVideo(id, WIDTH, HEIGHT, FOLDER_PATH);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}).handle((result, throwable) -> {
			if (throwable != null) {
				throw new CompletionException(throwable); // Wrap and rethrow the exception
			}
			return null;
		});
	}
}
