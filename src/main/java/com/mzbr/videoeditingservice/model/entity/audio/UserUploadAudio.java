package com.mzbr.videoeditingservice.model.entity.audio;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.mzbr.videoeditingservice.model.entity.Video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_upload_audio")
public class UserUploadAudio extends Audio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String url;

	@OneToOne
	@JoinColumn(name = "video_id")
	Video videoEntity;
}
