package com.mzbr.videoeditingservice.model.entity;

import javax.persistence.Entity;
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
@Table(name = "temp_crop")
public class TempCrop {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Integer x;
	Integer y;
	Integer width;
	Integer height;

	@OneToOne
	@JoinColumn(name = "temp_video_id")
	TempVideo tempVideo;

}
