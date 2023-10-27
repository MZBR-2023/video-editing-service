package com.mzbr.videoeditingservice.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_upload_audio")
public class UserUploadAudioEntity extends Audio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String url;

	@OneToOne
	@JoinColumn(name = "video_id")
	VideoEntity videoEntity;
}
