package com.mzbr.videoeditingservice;

import java.util.Set;
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
import com.mzbr.videoeditingservice.model.Crop;
import com.mzbr.videoeditingservice.model.Subtitle;
import com.mzbr.videoeditingservice.model.UserUploadAudioEntity;
import com.mzbr.videoeditingservice.model.VideoEntity;
import com.mzbr.videoeditingservice.repository.VideoRepository;
import com.mzbr.videoeditingservice.repository.VideoSegmentRepository;
import com.mzbr.videoeditingservice.service.EditingKinesisService;
import com.mzbr.videoeditingservice.service.VideoEditingService;

import lombok.RequiredArgsConstructor;

@SpringBootTest
@ActiveProfiles("ssafy")
@Profile("ssafy")
@ConfigurationPropertiesScan
@RequiredArgsConstructor
class VideoEditingServiceApplicationTests {



	@Autowired
	private LocalVideoEditingService localVideoEditingService;

	@Autowired
	EditingKinesisService kinesisConsumerService;

	@Qualifier("videoEditingServiceImpl")
	@Autowired
	private VideoEditingService videoEditingService;

	@Autowired
	private VideoSegmentRepository videoSegmentRepository;
	@Autowired
	private VideoRepository videoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void 로컬_테스트() throws Exception {
		Clip clip1 = Clip.builder()
			.id(1L)
			.url("test1.mp4")
			.name("video1")
			.durationTime(58000)
			.volume(1F)
			.build();
		Clip clip2 = Clip.builder()
			.id(2L)
			.url("test2.mp4")
			.name("video2")
			.durationTime(59000)
			.volume(0.1F)
			.width(720)
			.height(1280)
			.crop(Crop.builder().startX(100).startY(100).zoomFactor(2F).build())
			.build();
		Subtitle subtitle1 = Subtitle.builder()
			.color(16711680)
			.text("테스트1")
			.scale(2.0F)
			.startTime(1000)
			.endTime(5000)
			.id(1L)
			.positionX(100)
			.positionY(100)
			.zIndex(1)
			.build();

		Subtitle subtitle2	 = Subtitle.builder()
			.color(65280 )
			.text("good")
			.scale(0.6F)
			.startTime(3000)
			.endTime(8000)
			.id(1L)
			.positionX(100)
			.positionY(100)
			.zIndex(3)
			.build();
		UserUploadAudioEntity userUploadAudioEntity = UserUploadAudioEntity.builder()
			.id(1L)
			.url("test.mp3")
			.startTime(10000)
			.extension("mp3")
			.build();

		VideoEntity videoEntity = VideoEntity.builder()
			.clips(Set.of(clip1,clip2))
			.subtitles(Set.of(subtitle1,subtitle2))
			.userUploadAudioEntity(userUploadAudioEntity)
			.videoUuid(UUID.randomUUID().toString())
			.id(1L)
			.build();

		localVideoEditingService.processVideo(videoEntity,720,1280, "video");
	}

	@Test
	@Transactional
	void S3_테스트() throws Exception{
		videoEditingService.processVideo(1L,720,1280,"s3Test");

	}

	@Test
	@Transactional
	void 스트림_테스트() throws Exception{
		CompletableFuture<Void> voidCompletableFuture = kinesisConsumerService.updateAndProcessJob(
			"1");//비동기 처리 코드
		voidCompletableFuture.join();
	}
}
