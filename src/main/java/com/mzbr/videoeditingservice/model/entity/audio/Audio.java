package com.mzbr.videoeditingservice.model.entity.audio;

import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class Audio {
	private Integer startTime;
	private Integer endTime;
	private String extension;
	private Float volume;

	public abstract String getUrl();

	public Float getVolume() {
		return this.volume;
	}

	public String getExtension() {
		return this.extension;
	}

}
