package com.mzbr.videoeditingservice.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subtitle")
public class Subtitle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id")
	VideoEntity videoEntity;
}
