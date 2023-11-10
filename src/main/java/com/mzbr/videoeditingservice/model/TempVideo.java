package com.mzbr.videoeditingservice.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "temp_video")
@ToString
public class TempVideo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String videoName;
	private String originVideoUrl;
	private String afterCropUrl;

	@OneToOne(mappedBy = "tempVideo",fetch = FetchType.LAZY)
	TempCrop tempCrop;

	public void updateAfterCropUrl(String afterCropUrl) {
		this.afterCropUrl=afterCropUrl;
	}

	public void updateOriginCropUrl(String originVideoUrl) {
		this.originVideoUrl=originVideoUrl;
	}
}
