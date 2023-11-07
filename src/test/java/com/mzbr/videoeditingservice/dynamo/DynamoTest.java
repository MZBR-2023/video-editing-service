package com.mzbr.videoeditingservice.dynamo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import com.mzbr.videoeditingservice.model.VideoEncodingDynamoTable;
import com.mzbr.videoeditingservice.service.DynamoService;

@SpringBootTest
@ActiveProfiles("ssafy")
@Profile("ssafy")
public class DynamoTest {
	@Autowired
	private DynamoService dynamoService;
	@Test
	void 리스트_삽입_테스트(){
		List<VideoEncodingDynamoTable> videoEncodingDynamoTableList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			videoEncodingDynamoTableList.add(VideoEncodingDynamoTable.builder()
					.id(UUID.randomUUID().toString())
					.rdbId((long)i)
					.format("test")
					.status("wait")
				.build());
		}
		dynamoService.videoEncodingListBatchSave(videoEncodingDynamoTableList);
	}
}
