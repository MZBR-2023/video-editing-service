package com.mzbr.videoeditingservice.dto;

import java.util.List;

import lombok.Data;

@Data
public class VideoEditingRequestDto {
	String videoUuid;
	Long storeId;
	String description;
	List<String> tags;
	Integer star;
	uploadAudio audio;
	String thumbnailName;
	List<uploadClip> clips;
	List<uploadSubtitle> subtitles;

	@Data
	public static class uploadAudio {
		String fileName;
		Float volume;
	}
	@Data
	public static class uploadClip {
		String fileName;
		Float volume;
	}
	@Data
	public static class uploadSubtitle{
		String text;
		Float startDuration;
		Float endDuration;
		Integer x;
		Integer y;
		Float scale;
		Float rotation;
		Integer color;
		Integer zIndex;
	}
}
