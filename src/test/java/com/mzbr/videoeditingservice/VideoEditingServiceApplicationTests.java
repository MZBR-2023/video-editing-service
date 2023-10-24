package com.mzbr.videoeditingservice;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Crop;
import com.mzbr.videoeditingservice.model.VideoEntity;

@SpringBootTest
@Profile("ssafy")
class VideoEditingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void 비디오_병합_테스트() {
		Clip clip1 = Clip.builder()
			.id(1L)
			.url("test.mp4")
			.name("video1")
			.durationTime(11000)
			.build();
		Clip clip2 = Clip.builder()
			.id(2L)
			.url("test2.mp4")
			.name("video2")
			.durationTime(52000)
			.crop(Crop.builder().startX(0).startY(0).width(100).height(100).zoomFactor(1).build())
			.build();

		VideoEntity videoEntity = VideoEntity.builder()
			.clips(List.of(clip1,clip2))
			.id(1L)
			.build();

	}

}
