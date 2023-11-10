package com.mzbr.videoeditingservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mzbr.videoeditingservice.annotation.MemberId;
import com.mzbr.videoeditingservice.dto.TempPreviewDto;
import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;
import com.mzbr.videoeditingservice.dto.UrlDto;
import com.mzbr.videoeditingservice.service.TempVideoService;
import com.mzbr.videoeditingservice.service.VideoEditingService;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v/video")
@RequiredArgsConstructor
public class VideoController {
	private final TempVideoService tempVideoUploadService;
	private final VideoEditingService videoEditingService;
	private final S3Util s3Util;
	@PostMapping("/temp/upload")
	public ResponseEntity uploadTempVideo(@RequestBody UploadTempVideoDto uploadTempVideoDto,  @MemberId Integer memberId) {
		String url =  tempVideoUploadService.UploadTempVideo(uploadTempVideoDto, memberId);

		UrlDto urlDto = new UrlDto(uploadTempVideoDto.getVideoUuid(), url);
		return new ResponseEntity(urlDto, HttpStatus.CREATED);
	}

	@PostMapping("/temp/upload/{video-name}/upload-complete")
	public ResponseEntity uploadTempVideoComplete(@PathVariable(value = "video-name") String videoName,  @MemberId Integer memberId) throws Exception {
		String url = videoEditingService.tempVideoProcess(videoName,"crop");

		Map<String, String> response = new HashMap<>();
		response.put("url", s3Util.fileUrl(url));
		return new ResponseEntity(response, HttpStatus.CREATED);

	}

	@PostMapping("/preview-video")
	public ResponseEntity generateOrGetPreviewVideo(@RequestBody TempPreviewDto tempPreviewDto) throws Exception{
		String url = videoEditingService.processTempPreview(tempPreviewDto);

		Map<String, String> response = new HashMap<>();
		response.put("url", s3Util.fileUrl(url));
		return new ResponseEntity(response, HttpStatus.OK);
	}

	//영상 제작 시작
	@PostMapping("/{video-uuid}/process-start")
	public ResponseEntity videoProcessStart(@PathVariable(name = "video-uuid") String videoUuid, @MemberId Integer memberId) {
		videoEditingService.videoProcessStart(memberId, videoUuid);

		return new ResponseEntity(HttpStatus.CREATED);
	}

	//영상 제작 완료

}
