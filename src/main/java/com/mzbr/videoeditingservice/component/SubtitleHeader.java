package com.mzbr.videoeditingservice.component;import org.springframework.boot.context.properties.ConfigurationProperties;import org.springframework.stereotype.Component;import lombok.Getter;import lombok.Setter;@ConfigurationProperties("subtitle")@Getterpublic class SubtitleHeader {	private String assHeader;	public void setAssHeader(String assHeader) {		System.out.println("!!!!!!!!!!!!!!!!!"+assHeader);		this.assHeader = assHeader;	}}