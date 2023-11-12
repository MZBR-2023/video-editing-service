package com.mzbr.videoeditingservice.service;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mzbr.videoeditingservice.dto.PreVideoEditingRequestDto;
import com.mzbr.videoeditingservice.dto.PreVideoEditingResponseDto;
import com.mzbr.videoeditingservice.dto.UploadTempVideoDto;
import com.mzbr.videoeditingservice.exception.MemberException;
import com.mzbr.videoeditingservice.model.TempCrop;
import com.mzbr.videoeditingservice.model.TempVideo;
import com.mzbr.videoeditingservice.model.VideoEntity;
import com.mzbr.videoeditingservice.repository.TempCropRepository;
import com.mzbr.videoeditingservice.repository.TempVideoRepository;
import com.mzbr.videoeditingservice.repository.VideoRepository;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreVideoServiceImpl implements PreVideoService {
	private final S3Util s3Util;

	private final TempVideoRepository tempVideoRepository;
	private final TempCropRepository tempCropRepository;
	private final VideoRepository videoRepository;
	@Override
	@Transactional
	public String uploadTempVideo(UploadTempVideoDto uploadTempVideoDto, Integer memberId) {
		VideoEntity videoEntity = videoRepository.findByVideoUuid(uploadTempVideoDto.getVideoUuid()).orElseThrow();
		if (videoEntity.getMember().getId() != memberId) {
			throw new MemberException("사용자의 엔티티가 아닙니다.");
		}

		TempVideo tempVideo = TempVideo.builder()
			.videoName(uploadTempVideoDto.getVideoName())
			.videoEntity(videoEntity)
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

	@Override
	public PreVideoEditingResponseDto getThumbnailAndAudioUploadPresignUrl(PreVideoEditingRequestDto preVideoEditingRequestDto) {
		PreVideoEditingResponseDto preVideoEditingResponseDto = new PreVideoEditingResponseDto();
		preVideoEditingResponseDto.setAudioUrl(s3Util.generatePresignedUrl("audio/"+preVideoEditingRequestDto.getAudioFileName()));
		preVideoEditingResponseDto.setThumbnailUrl(s3Util.generatePresignedUrl("thumbnail/"+preVideoEditingRequestDto.getThumbnailFileName()));
		return preVideoEditingResponseDto;
	}

}
