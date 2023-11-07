package com.mzbr.videoeditingservice.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.mzbr.videoeditingservice.model.Audio;
import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Subtitle;
import com.mzbr.videoeditingservice.model.VideoEntity;

public interface VideoEditingService {


	String processVideo(Long videoId, int width, int height, String folderPath) throws Exception;

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
