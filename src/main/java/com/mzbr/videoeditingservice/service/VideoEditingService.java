package com.mzbr.videoeditingservice.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.mzbr.videoeditingservice.dto.TempPreviewDto;
import com.mzbr.videoeditingservice.dto.VideoEditingRequestDto;
import com.mzbr.videoeditingservice.model.Audio;
import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Subtitle;

public interface VideoEditingService {

	void videoProcessStart(Integer memberId, String videoUuid);

	@Transactional
	void videoEditing(VideoEditingRequestDto videoEditingRequestDto, Integer memberId);

	String processVideo(Long videoId, int width, int height, String folderPath) throws Exception;
	String tempVideoProcess(String videoName, String folderPath, Integer memberId) throws Exception;

	String processTempPreview(TempPreviewDto tempPreviewDto, Integer memberId) throws Exception;
	List<Input> prepareVideoInputs(Set<Clip> clips) throws Exception;

	String generateVideoCropAndLayoutFilter(Set<Clip> clips, Integer scaleX, Integer scaleY) throws Exception;

	String generateVideoVolumeFilter(Set<Clip> clips) throws Exception;

	String generateAudioFilter(Audio audio, int totalDurationTime, int clipCount) throws Exception;

	String generateConcatVideoFilter(Integer clipCount, boolean hasAudio) throws Exception;

	Input insertAudioToVideo(Audio audio) throws Exception;

	String generateASSBySubtitles(Set<Subtitle> subtitles, String fileName) throws Exception;

	void executeSplitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec, String filter, String outputPath) throws
		Exception;

	void uploadTempFileToS3(List<Path> pathList, String folderName) throws Exception;

	void deleteTemporaryFile(List<Path> pathList, String assPath) throws Exception;
}
