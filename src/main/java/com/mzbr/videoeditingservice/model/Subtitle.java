package com.mzbr.videoeditingservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subtitle {

	@Id
	Long id;

	String text;
	Integer startTime;
	Integer endTime;

	Integer positionX;
	Integer positionY;
	Float scale;
	Float rotation;
	Integer color;
	Integer zIndex;

	@ManyToOne
	@JoinColumn(name = "video_id")
	VideoEntity videoEntity;
}
