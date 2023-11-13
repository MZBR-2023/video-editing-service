package com.mzbr.videoeditingservice.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;


import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FilterGraph;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.mzbr.videoeditingservice.dto.TempPreviewDto;
import com.mzbr.videoeditingservice.dto.VideoEditingRequestDto;
import com.mzbr.videoeditingservice.model.entity.audio.Audio;
import com.mzbr.videoeditingservice.model.entity.Clip;
import com.mzbr.videoeditingservice.model.entity.Subtitle;

public interface VideoEditingService {

	void videoProcessStart(Integer memberId, String videoUuid);

	void videoEditing(VideoEditingRequestDto videoEditingRequestDto, Integer memberId);

	void processVideo(Long videoId, int width, int height, String folderPath) throws Exception;
	String tempVideoProcess(String videoName, String folderPath, Integer memberId) throws Exception;

	String processTempPreview(TempPreviewDto tempPreviewDto, Integer memberId) throws Exception;
	List<Input> prepareVideoInputs(Set<Clip> clips) throws Exception;


	Input insertAudioToVideo(Audio audio) throws Exception;

	Path generateASSBySubtitles(Set<Subtitle> subtitles, String fileName) throws Exception;



	void executeSplitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec, FilterGraph filter, String outputPath) throws
		Exception;

	void uploadTempFileToS3(List<Path> pathList, String folderName) throws Exception;

	void deleteTemporaryFile(List<Path> pathList) throws Exception;
}
