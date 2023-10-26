package com.mzbr.videoeditingservice;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Crop;
import com.mzbr.videoeditingservice.model.Subtitle;
import com.mzbr.videoeditingservice.model.UserUploadAudioEntity;
import com.mzbr.videoeditingservice.model.VideoEntity;

@SpringBootTest
@Profile("ssafy")
@ConfigurationPropertiesScan
class VideoEditingServiceApplicationTests {


	private final LocalVideoEditingService localVideoEditingService=new LocalVideoEditingService();

	@Test
	void contextLoads() {
	}

	@Test
	void 비디오_병합_테스트() throws Exception {
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
			.crop(Crop.builder().startX(100).startY(100).width(100).height(100).zoomFactor(2).build())
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
			.clips(List.of(clip1,clip2))
			.subtitles((List.of(subtitle1,subtitle2)))
			.userUploadAudioEntity(userUploadAudioEntity)
			.videoUuid("uuid")
			.id(1L)
			.build();
		// videoEditingService.processVideo(videoEntity,"out.mp4");

		localVideoEditingService.processVideo(videoEntity,720,1280);
	}

}
