package com.mzbr.videoeditingservice.kinesis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

@SpringBootTest
@ActiveProfiles("ssafy")
public class KinesisProducerTest {

	@Value("${cloud.aws.kinesis.consumer-name}")
	private String STREAM_NAME;
	private String partitionKey = "test"; // 파티션 키를 지정합니다.

	@Autowired
	private KinesisClient kinesisClient;

	@Test
	public void testPutRecord() {

		// 보낼 메시지를 설정합니다.
		String message = "1";
		SdkBytes data = SdkBytes.fromUtf8String(message);

		// PutRecord 요청을 생성합니다.
		PutRecordRequest putRecordRequest = PutRecordRequest.builder()
			.streamName(STREAM_NAME)
			.data(data)
			.partitionKey(partitionKey)
			.build();

		// 데이터를 스트림에 발행합니다.
		PutRecordResponse putRecordResponse = kinesisClient.putRecord(putRecordRequest);

		// 응답을 검증합니다.
		assertNotNull(putRecordResponse.sequenceNumber());

		// 리소스를 해제합니다.
		kinesisClient.close();
	}
}
