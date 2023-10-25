package com.mzbr.videoeditingservice.service;import java.io.IOException;import java.util.List;import com.github.kokorin.jaffree.ffmpeg.FFmpeg;import com.github.kokorin.jaffree.ffmpeg.Input;import com.mzbr.videoeditingservice.model.Audio;import com.mzbr.videoeditingservice.model.Clip;import com.mzbr.videoeditingservice.model.VideoEntity;public interface VideoEditingService {	String processVideo(VideoEntity videoEntity, String outPutPath);	void localVideoProcess(VideoEntity videoEntity, int width, int height) throws IOException;	List<Input> prepareVideoInputs(List<Clip> clips);	String generateVideoCropAndLayoutFilter(List<Clip> clips, Integer scaleX, Integer scaleY);	String generateAudioVolumeFilter(List<Clip> clips);	String generateConcatVideoFilter(Integer clipCount);	Input insertAudioToVideo(Audio audio);	// String generateSubtitleInsertionFilter();	List<String> splitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec);	void uploadTempFileToS3(List<String> fileLocations);	void deleteTemporaryFile(List<String> fileLocations);}