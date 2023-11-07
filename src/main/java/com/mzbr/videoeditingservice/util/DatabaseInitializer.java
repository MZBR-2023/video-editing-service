package com.mzbr.videoeditingservice.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Crop;
import com.mzbr.videoeditingservice.model.Subtitle;
import com.mzbr.videoeditingservice.model.UserUploadAudioEntity;
import com.mzbr.videoeditingservice.model.VideoEntity;
import com.mzbr.videoeditingservice.repository.ClipRepository;
import com.mzbr.videoeditingservice.repository.CropRepository;
import com.mzbr.videoeditingservice.repository.SubtitleRepository;
import com.mzbr.videoeditingservice.repository.UserUploadAudioRepository;
import com.mzbr.videoeditingservice.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Profile("ssafy")
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {
	private final VideoRepository videoRepository;
	private final SubtitleRepository subtitleRepository;
	private final ClipRepository clipRepository;
	private final CropRepository cropRepository;
	private final UserUploadAudioRepository userUploadAudioRepository;
	@PostConstruct
	@Transactional
	public void init() {
		VideoEntity videoEntity = VideoEntity.builder()
			.videoUuid(UUID.randomUUID().toString())
			.build();
		videoRepository.save(videoEntity);
		Subtitle subtitle1 = Subtitle.builder()
			.videoEntity(videoEntity)
			.color(16711680)
			.text("테스트1")
			.scale(2.0F)
			.startTime(1000)
			.endTime(5000)
			.positionX(100)
			.positionY(100)
			.zIndex(1)
			.build();

		Subtitle subtitle2	 = Subtitle.builder()
			.videoEntity(videoEntity)
			.color(65280 )
			.text("good")
			.scale(0.6F)
			.startTime(3000)
			.endTime(8000)
			.positionX(100)
			.positionY(100)
			.zIndex(3)
			.build();

		subtitleRepository.saveAll(List.of(subtitle1, subtitle2));




		Clip clip1 = Clip.builder()
			.videoEntity(videoEntity)
			.url("origin_video/test1.mp4")
			.name("video1")
			.durationTime(58000)
			.volume(1F)
			.build();
		Clip clip2 = Clip.builder()
			.videoEntity(videoEntity)
			.url("origin_video/test2.mp4")
			.name("video2")
			.durationTime(59000)
			.volume(0.1F)
			.width(720)
			.height(1280)
			.build();

		clipRepository.saveAll(List.of(clip1, clip2));

		Crop crop = Crop.builder().startX(100).startY(100).zoomFactor(2F).clip(clip2).build();

		cropRepository.save(crop);
		UserUploadAudioEntity userUploadAudioEntity= UserUploadAudioEntity.builder()
			.videoEntity(videoEntity)
			.url("origin_audio/test.mp3")
			.startTime(10000)
			.volume(1.5F)
			.extension("mp3")
			.build();

		userUploadAudioRepository.save(userUploadAudioEntity);



	}
}
