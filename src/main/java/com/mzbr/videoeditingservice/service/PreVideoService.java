package com.mzbr.videoeditingservice.service;

import com.mzbr.videoeditingservice.dto.PreVideoEditingRequestDto;
import com.mzbr.videoeditingservice.dto.PreVideoEditingResponseDto;
import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;

public interface PreVideoService {


	String uploadTempVideo(UploadTempVideoDto uploadTempVideoDto, Integer memberId);


	PreVideoEditingResponseDto getThumbnailAndAudioUploadPresignUrl(PreVideoEditingRequestDto preVideoEditingRequestDto);
}
