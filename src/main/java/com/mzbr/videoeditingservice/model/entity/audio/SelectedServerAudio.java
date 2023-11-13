package com.mzbr.videoeditingservice.model.entity.audio;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "selected_server_audio")
public class SelectedServerAudio extends Audio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@OneToOne
	@JoinColumn(name = "video_id")
	Video videoEntity;

	@ManyToOne
	@JoinColumn(name = "server_audio_id")
	ServerAudio serverAudioEntity;

	@Override
	public String getUrl() {
		return serverAudioEntity.getUrl();
	}
}
