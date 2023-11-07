package com.mzbr.videoeditingservice.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clip")
public class Clip {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String name;
	String url;

	@OneToOne(mappedBy = "clip")
	Crop crop;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id")
	VideoEntity videoEntity;

	Integer width;
	Integer height;

	Integer durationTime;
	Float volume;
	String extension;
}
