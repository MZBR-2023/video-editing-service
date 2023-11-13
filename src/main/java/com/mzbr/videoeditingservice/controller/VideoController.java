package com.mzbr.videoeditingservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mzbr.videoeditingservice.annotation.MemberId;
import com.mzbr.videoeditingservice.dto.PreVideoEditingRequestDto;
import com.mzbr.videoeditingservice.dto.PreVideoEditingResponseDto;
import com.mzbr.videoeditingservice.dto.TempPreviewDto;
import com.mzbr.videoeditingservice.dto.UploadCompleteRequestDto;
import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;
import com.mzbr.videoeditingservice.dto.UrlDto;
import com.mzbr.videoeditingservice.dto.VideoEditingRequestDto;
import com.mzbr.videoeditingservice.service.PreVideoService;
import com.mzbr.videoeditingservice.service.VideoEditingService;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v/video")
@RequiredArgsConstructor
public class VideoController {
	private final PreVideoService preVideoService;
	private final VideoEditingService videoEditingService;
	private final S3Util s3Util;

	@PostMapping("/temp/upload")
	public ResponseEntity uploadTempVideo(@RequestBody UploadTempVideoDto uploadTempVideoDto,
		@MemberId Integer memberId) {
		String url = preVideoService.uploadTempVideo(uploadTempVideoDto, memberId);

		UrlDto urlDto = new UrlDto(url);
		return new ResponseEntity(urlDto, HttpStatus.CREATED);
	}

	@PostMapping("/temp/upload-complete")
	public ResponseEntity uploadTempVideoComplete(@RequestBody UploadCompleteRequestDto uploadCompleteRequestDto,
		@MemberId Integer memberId) throws Exception {
		String url = videoEditingService.tempVideoProcess(uploadCompleteRequestDto.getVideoName(), "crop", memberId);

		UrlDto urlDto = new UrlDto(s3Util.fileUrl(url));
		return new ResponseEntity(urlDto, HttpStatus.OK);

	}

	@PostMapping("/preview-video")
	public ResponseEntity generateOrGetPreviewVideo(@RequestBody TempPreviewDto tempPreviewDto,
		@MemberId Integer memberId) throws Exception {
		String url = videoEditingService.processTempPreview(tempPreviewDto, memberId);

		Map<String, String> response = new HashMap<>();
		response.put("url", s3Util.fileUrl(url));
		return new ResponseEntity(response, HttpStatus.OK);
	}

	//영상 제작 시작
	@PostMapping("/{video-uuid}/process-start")
	public ResponseEntity videoProcessStart(@PathVariable(name = "video-uuid") String videoUuid,
		@MemberId Integer memberId) {
		videoEditingService.videoProcessStart(memberId, videoUuid);

		return new ResponseEntity(HttpStatus.CREATED);
	}

	//영상 제작 완료 전 오디오와 썸네일 전송
	@PostMapping("/thumbnail-and-audio-upload-url")
	public ResponseEntity getThumbnailAndAudioUploadUrl(
		@RequestBody PreVideoEditingRequestDto preVideoEditingRequestDto) {
		PreVideoEditingResponseDto preVideoEditingResponseDto = preVideoService.getThumbnailAndAudioUploadPresignUrl(
			preVideoEditingRequestDto);
		return new ResponseEntity(preVideoEditingResponseDto, HttpStatus.OK);
	}

	//영상 제작 완료
	@PostMapping("/edit")
	public ResponseEntity videoEditProcessStart(@RequestBody VideoEditingRequestDto videoEditingRequestDto,
		@MemberId Integer memberId) {
		videoEditingService.videoEditing(videoEditingRequestDto, memberId);
		return new ResponseEntity(HttpStatus.OK);
	}

}
