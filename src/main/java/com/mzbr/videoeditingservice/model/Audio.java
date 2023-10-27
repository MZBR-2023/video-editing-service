package com.mzbr.videoeditingservice.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
