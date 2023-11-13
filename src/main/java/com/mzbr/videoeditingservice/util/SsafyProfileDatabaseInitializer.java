package com.mzbr.videoeditingservice.util;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mzbr.videoeditingservice.model.entity.Clip;
import com.mzbr.videoeditingservice.model.entity.Subtitle;
import com.mzbr.videoeditingservice.model.entity.audio.UserUploadAudio;
import com.mzbr.videoeditingservice.model.entity.Video;
import com.mzbr.videoeditingservice.repository.ClipRepository;
import com.mzbr.videoeditingservice.repository.SubtitleRepository;
import com.mzbr.videoeditingservice.repository.UserUploadAudioRepository;
import com.mzbr.videoeditingservice.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Profile("ssafy")
@Component
@RequiredArgsConstructor
public class SsafyProfileDatabaseInitializer {
	private final VideoRepository videoRepository;
	private final SubtitleRepository subtitleRepository;
	private final ClipRepository clipRepository;
	private final UserUploadAudioRepository userUploadAudioRepository;
	@PostConstruct
	@Transactional
	public void init() {
		Video videoEntity = Video.builder()
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

		Subtitle subtitle2 = Subtitle.builder()
			.videoEntity(videoEntity)
			.color(65280)
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
			.url("crop/0538530a-5377-4387-ad74-8034ed7ae5c1.mp4")
			.name("video1")
			.volume(1F)
			.build();
		Clip clip2 = Clip.builder()
			.videoEntity(videoEntity)
			.url("crop/09e12226-15a4-4efb-bd9d-41e31726929b.mp4")
			.name("video2")
			.volume(0.1F)
			.build();

		clipRepository.saveAll(List.of(clip1, clip2));

		UserUploadAudio userUploadAudioEntity = UserUploadAudio.builder()
			.videoEntity(videoEntity)
			.url("audio/1.mp3")
			.startTime(10000)
			.volume(1.5F)
			.extension("mp3")
			.build();

		userUploadAudioRepository.save(userUploadAudioEntity);



	}
}
