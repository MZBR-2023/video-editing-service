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

import com.mzbr.videoeditingservice.dto.TempPreviewDto;
import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;
import com.mzbr.videoeditingservice.service.TempVideoService;
import com.mzbr.videoeditingservice.service.VideoEditingService;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/temp-video")
@RequiredArgsConstructor
public class TempVideoController {
	private final TempVideoService tempVideoUploadService;
	private final VideoEditingService videoEditingService;
	private final S3Util s3Util;
	@PostMapping("/upload")
	public ResponseEntity uploadTempVideo(@RequestBody UploadTempVideoDto uploadTempVideoDto) {
		String url =  tempVideoUploadService.UploadTempVideo(uploadTempVideoDto);

		Map<String, String> response = new HashMap<>();
		response.put("url", url);
		return new ResponseEntity(response, HttpStatus.CREATED);
	}

	@PostMapping("/upload/{videoName}/upload-complete")
	public ResponseEntity uploadTempVideoComplete(@PathVariable String videoName) throws Exception {
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
}
