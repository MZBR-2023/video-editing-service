package com.mzbr.videoeditingservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoEditResponseDto {

	List<String> segmentUrls;


}
