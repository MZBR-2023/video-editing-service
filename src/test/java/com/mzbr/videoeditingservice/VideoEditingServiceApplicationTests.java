package com.mzbr.videoeditingservice;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Subtitle;
import com.mzbr.videoeditingservice.model.UserUploadAudioEntity;
import com.mzbr.videoeditingservice.model.VideoEntity;
import com.mzbr.videoeditingservice.repository.ClipRepository;
import com.mzbr.videoeditingservice.repository.SubtitleRepository;
import com.mzbr.videoeditingservice.repository.UserUploadAudioRepository;
import com.mzbr.videoeditingservice.repository.VideoRepository;
import com.mzbr.videoeditingservice.service.EditingConsumerKinesisService;
import com.mzbr.videoeditingservice.service.VideoEditingService;

import lombok.RequiredArgsConstructor;

@SpringBootTest
@ActiveProfiles("ssafy")
@Profile("ssafy")
@ConfigurationPropertiesScan
class VideoEditingServiceApplicationTests {

	@Autowired
	EditingConsumerKinesisService kinesisConsumerService;

	@Qualifier("videoEditingServiceImpl")
	@Autowired
	private VideoEditingService videoEditingService;

	@Autowired
	private VideoRepository videoRepository;

	@Autowired
	private SubtitleRepository subtitleRepository;
	@Autowired
	private ClipRepository clipRepository;
	@Autowired
	private UserUploadAudioRepository userUploadAudioRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void S3_테스트() throws Exception {

		videoEditingService.processVideo(3L, 720, 1280, "s3Test");

	}

	@Test
	@Transactional
	void 스트림_테스트() throws Exception {
		CompletableFuture<Void> voidCompletableFuture = kinesisConsumerService.updateAndProcessJob(
			"1");//비동기 처리 코드
		voidCompletableFuture.join();
	}
}
