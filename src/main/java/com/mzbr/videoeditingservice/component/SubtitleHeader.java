package com.mzbr.videoeditingservice.component;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("subtitle")
@Getter
@Setter
public class SubtitleHeader {
	private String assHeader;

	public void setAssHeader(String assHeader) {
		this.assHeader = assHeader;
	}
}
