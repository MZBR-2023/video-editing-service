package com.mzbr.videoeditingservice.model.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "video_data")
public class VideoData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Integer star;
	String description;
	String thumbnailUrl;
	String masterUrl;
	String P144Url;
	String P360Url;
	String P480Url;
	String P720Url;




	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id")
	Video videoEntity;

}
