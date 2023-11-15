package com.mzbr.videoeditingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"ssafy", "prod"})
public class EditingConsumerKinesisService {
	private final KinesisAsyncClient kinesisAsyncClient;
	private final KinesisClient kinesisClient;
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

	private final Semaphore permits = new Semaphore(2);

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
				.limit(2)
				.build());
		getRecordsFuture.thenAcceptAsync(getRecordsResponse -> {
			getRecordsResponse.records().forEach(record -> {
				try {
					permits.acquire();
					String data = StandardCharsets.UTF_8.decode(record.data().asByteBuffer()).toString();
					updateAndProcessJob(data)
						.whenComplete((result, throwable) -> {
							permits.release();
							if (throwable != null) {
								log.error("{} 작업을 제대로 처리하지 못 했습니다.", data);
							}
						});

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
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


	public CompletableFuture<Void> updateAndProcessJob(String idString) {
		log.info("{} 작업 시작",idString);
		Long id = Long.parseLong(idString);
		return CompletableFuture.supplyAsync(() -> {
			GetItemResponse getItemResponse = dynamoService.getItemResponse(JOB_TABLE, JOB_ID, id);
			if (getItemResponse.item() == null || getItemResponse.item().isEmpty()) {
				throw new CompletionException(new NoSuchElementException("조건을 만족하지 않는 작업: " + id));
			}
			AttributeValue statusValue = getItemResponse.item().get(STATUS);
			if (statusValue == null || !WAITING_STATUS.equals(statusValue.s())) {
				throw new CompletionException(new NoSuchElementException("조건을 만족하지 않는 작업: " + id));
			}
			if (updateStatusToInProgressIfWaiting(id)) {
				throw new CompletionException(new NoSuchElementException("조건을 만족하지 않는 작업: " + id));
			}
			return id;
		}).thenCompose(result -> processJob(id)).thenRun(() -> {
			updateJobStatus(id, COMPLETED_STATUS);
		}).exceptionally(e -> {
			if (e.getCause() instanceof NoSuchElementException) {
				return null;
			}

			updateJobStatus(id, FAILED_STATUS);
			return null;
		});

	}

	private boolean updateStatusToInProgressIfWaiting(Long id) {
		UpdateItemResponse updateItemResponse = updateJobStatus(id, IN_PROGRESS_STATUS);
		AttributeValue oldStatusValue = updateItemResponse.attributes().get(STATUS);
		return oldStatusValue == null || !WAITING_STATUS.equals(oldStatusValue.s());
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
