package com.mzbr.videoeditingservice.service;

import org.springframework.transaction.annotation.Transactional;

import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;

public interface TempVideoService {


	@Transactional
	String UploadTempVideo(UploadTempVideoDto uploadTempVideoDto, Integer memberId);
}
