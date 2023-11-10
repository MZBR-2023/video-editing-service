package com.mzbr.videoeditingservice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mzbr.videoeditingservice.model.Clip;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Util {
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${cloud.aws.s3.bucket}")
	private String BUCKET_NAME;

	public void uploadLocalFileByStringFormat(List<Path> pathList, String folderName) {
		for (Path path : pathList) {
			uploadLocalFile(path,folderName + "/" + path.getFileName().toString());
		}
	}
	public String fileUrl(String fileName){
		return "https://" + BUCKET_NAME + ".s3." + Region.AP_NORTHEAST_2.toString() + ".amazonaws.com/" + fileName;
	}
	public String uploadLocalFile(Path path,String uploadUrl){
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(uploadUrl)
			.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromFile(path.toFile()));

		return uploadUrl;
	}

	public Map<Long, String> getClipsPresignedUrlMap(Set<Clip> clips) {
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

	public String generatePresignedUrl(String objectName) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(objectName)
			.build();

		PresignedPutObjectRequest presignedPutObjectRequest =
			s3Presigner.presignPutObject(z -> z.signatureDuration(Duration.ofMinutes(10)).putObjectRequest(putObjectRequest));

		URL url = presignedPutObjectRequest.url();


		return url.toString();
	}

	public Path getFileToLocalDirectory(String fileName) throws IOException {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(BUCKET_NAME)
			.key(fileName)
			.build()
			;
		ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
		byte[] data = objectBytes.asByteArray();

		File myFile = new File(UUID.randomUUID().toString()+".mp4");
		OutputStream os = new FileOutputStream(myFile);
		os.write(data);
		os.close();
		return myFile.getAbsoluteFile().toPath();
	}
}
