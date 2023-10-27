package com.mzbr.videoeditingservice.util;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mzbr.videoeditingservice.model.Clip;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Util {
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${cloud.aws.s3.bucket}")
	private String BUCKET_NAME;

	public void uploadLocalFileByStringFormat(List<Path> pathList, String folderName) {
		for (Path path : pathList) {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(BUCKET_NAME)
				.key(folderName + "/" + path.getFileName().toString())
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromFile(path.toFile()));
		}
	}

	public Map<Long, String> getClipsPresignedUrlMap(List<Clip> clips) {
		Map<Long, String> resultMap = new HashMap<>();
		for (Clip clip : clips) {
			resultMap.put(clip.getId(), getPresigndUrl(clip.getUrl()));
		}

		return resultMap;
	}

	public String getPresigndUrl(String url) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(url)
			.build();

		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(5))
			.getObjectRequest(getObjectRequest)
			.build();

		PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
		return presignedGetObjectRequest.url().toString();
	}
}
