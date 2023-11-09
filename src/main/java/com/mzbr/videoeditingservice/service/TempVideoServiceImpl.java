package com.mzbr.videoeditingservice.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;
import com.mzbr.videoeditingservice.model.TempCrop;
import com.mzbr.videoeditingservice.model.TempVideo;
import com.mzbr.videoeditingservice.repository.TempCropRepository;
import com.mzbr.videoeditingservice.repository.TempVideoRepository;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TempVideoServiceImpl implements TempVideoService {
	private final S3Util s3Util;

	private final TempVideoRepository tempVideoRepository;
	private final TempCropRepository tempCropRepository;

	@Override
	@Transactional
	public String UploadTempVideo(UploadTempVideoDto uploadTempVideoDto) {
		TempVideo tempVideo = TempVideo.builder()
			.videoName(uploadTempVideoDto.getVideoName())
			.build();

		tempVideoRepository.save(tempVideo);

		if (uploadTempVideoDto.getCrop() != null) {
			TempCrop tempCrop = TempCrop.builder()
					.x(uploadTempVideoDto.getCrop().getX())
				.y(uploadTempVideoDto.getCrop().getY())
				.width(uploadTempVideoDto.getCrop().getWidth())
				.height(uploadTempVideoDto.getCrop().getHeight())
				.tempVideo(tempVideo)
				.build();
			tempCropRepository.save(tempCrop);
		}

		String url =  s3Util.generatePresignedUrl("temp/"+uploadTempVideoDto.getVideoName());
		tempVideo.updateOriginCropUrl("temp/"+uploadTempVideoDto.getVideoName());


		return url;
	}

}
