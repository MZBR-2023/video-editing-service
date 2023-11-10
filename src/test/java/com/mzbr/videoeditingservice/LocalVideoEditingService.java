package com.mzbr.videoeditingservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.mzbr.videoeditingservice.component.SubtitleHeader;
import com.mzbr.videoeditingservice.model.Audio;
import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.VideoEntity;
import com.mzbr.videoeditingservice.repository.TempPreviewRepository;
import com.mzbr.videoeditingservice.repository.TempVideoRepository;
import com.mzbr.videoeditingservice.repository.VideoRepository;
import com.mzbr.videoeditingservice.repository.VideoSegmentRepository;
import com.mzbr.videoeditingservice.service.DynamoService;
import com.mzbr.videoeditingservice.service.KinesisProducerService;
import com.mzbr.videoeditingservice.service.VideoEditingServiceImpl;
import com.mzbr.videoeditingservice.util.S3Util;


@Component("LocalVideoEditing")

public class LocalVideoEditingService extends VideoEditingServiceImpl {
	protected static final Logger log = LoggerFactory.getLogger(VideoEditingServiceImpl.class);
	private final ResourceLoader resourceLoader;

	@Autowired
	public LocalVideoEditingService(S3Util s3Util, SubtitleHeader subtitleHeader, VideoSegmentRepository videoSegmentRepository,
		VideoRepository videoRepository,
		ResourceLoader resourceLoader, DynamoService dynamoService, KinesisProducerService kinesisProducerService, TempVideoRepository tempVideoRepository, TempPreviewRepository tempPreviewRepository) {
		super(s3Util, subtitleHeader, videoSegmentRepository,videoRepository, dynamoService, kinesisProducerService, tempVideoRepository, tempPreviewRepository);
		this.resourceLoader = resourceLoader;
	}

	public String processVideo(VideoEntity videoEntity, int width, int height, String folderPath) throws Exception {
		boolean hasVideo =
			videoEntity.getUserUploadAudioEntity() != null || videoEntity.getSelectedServerAudioEntity() != null;

		String outputPath = videoEntity.getVideoUuid() + "[%03d].mov";

		try {
			FFmpeg fFmpeg = FFmpeg.atPath();
			String assPath = generateASSBySubtitles(videoEntity.getSubtitles(), videoEntity.getVideoUuid());
			StringBuilder filter = new StringBuilder();
			List<Input> inputs = prepareVideoInputs(videoEntity.getClips());
			filter.append(generateVideoCropAndLayoutFilter(videoEntity.getClips(), width, height)).append(";");
			filter.append(generateVideoVolumeFilter(videoEntity.getClips())).append(";");

			filter.append(generateConcatVideoFilter(videoEntity.getClips().size(), hasVideo)).append(";");

			filter.append("[v_concat]ass=" + modifyWindowPathForFfmpeg(assPath) + "[outv]");
			inputs.add(insertAudioToVideo(videoEntity.getUserUploadAudioEntity()));

			inputs.forEach(input -> fFmpeg.addInput(input));

			executeSplitVideoIntoSegments(fFmpeg, 5, filter.toString(), outputPath);

			List<Path> pathList = getSegementPathList(videoEntity.getVideoUuid());

			uploadTempFileToS3(pathList, folderPath + videoEntity.getVideoUuid());
			deleteTemporaryFile(pathList, assPath);

		} catch (IOException e) {
			log.debug(e.getMessage());

		}

		return null;
	}


	@Override
	public List<Input> prepareVideoInputs(Set<Clip> clips) throws Exception {

		List<Input> inputs = new ArrayList<>();

		for (Clip clip : clips) {
			Resource resource = resourceLoader.getResource("classpath:" + clip.getUrl());
			File file = null;
			try {
				file = File.createTempFile(clip.getUrl(), "." + clip.getExtension());
			} catch (IOException e) {
				log.debug("{} 파일을 불러오는데 실패했습니다.", clip.getUrl());
				throw new IOException("파일을 찾을 수 없습니다.");
			}

			Files.copy(resource.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			inputs.add(UrlInput.fromPath(file.toPath()));
		}

		return inputs;
	}

	@Override
	public Input insertAudioToVideo(Audio audio) throws Exception {
		try {
			Resource resource = resourceLoader.getResource("classpath:" + audio.getUrl());
			File file = File.createTempFile(audio.getUrl(), "." + audio.getExtension());
			Files.copy(resource.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return UrlInput.fromPath(file.toPath());

		} catch (IOException e) {
			log.debug("{}파일을 불러오는데 실패했습니다.", audio.getUrl());
			throw new IOException("파일을 찾을 수 없습니다.");
		}
	}

}
