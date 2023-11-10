package com.mzbr.videoeditingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlDto {

	@NotNull
	String url;
}
