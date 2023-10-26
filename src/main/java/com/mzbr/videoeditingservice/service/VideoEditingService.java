package com.mzbr.videoeditingservice.service;import java.io.IOException;import java.util.List;import com.github.kokorin.jaffree.ffmpeg.FFmpeg;import com.github.kokorin.jaffree.ffmpeg.Input;import com.mzbr.videoeditingservice.model.Audio;import com.mzbr.videoeditingservice.model.Clip;import com.mzbr.videoeditingservice.model.Subtitle;import com.mzbr.videoeditingservice.model.VideoEntity;public interface VideoEditingService {	String processVideo(VideoEntity videoEntity, int width, int height) throws Exception;	List<Input> prepareVideoInputs(List<Clip> clips) throws Exception;	String generateVideoCropAndLayoutFilter(List<Clip> clips, Integer scaleX, Integer scaleY) throws Exception;	String generateAudioVolumeFilter(List<Clip> clips) throws Exception;	String generateConcatVideoFilter(Integer clipCount) throws Exception;	Input insertAudioToVideo(Audio audio) throws Exception;	// String generateSubtitleInsertionFilter();	String generateASSBySubtitles(List<Subtitle> subtitles, String fileName) throws Exception;	List<String> splitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec) throws Exception;	void uploadTempFileToS3(List<String> fileLocations) throws Exception;	void deleteTemporaryFile(List<String> fileLocations) throws Exception;}