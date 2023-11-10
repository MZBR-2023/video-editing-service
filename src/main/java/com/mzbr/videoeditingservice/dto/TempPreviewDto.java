package com.mzbr.videoeditingservice.dto;

import java.util.List;

import lombok.Data;

@Data
public class TempPreviewDto {
	String versionId;
	List<String> videoNameList;
}
