package com.mzbr.videoeditingservice.dto;

import lombok.Data;

@Data
public class UploadTempVideoDto {
	String videoName;
	String videoUuid;
	Crop crop;


	@Data
	public static class Crop {
		Integer x;
		Integer y;
		Integer width;
		Integer height;
	}
}
