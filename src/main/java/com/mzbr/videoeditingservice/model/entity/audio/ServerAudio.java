package com.mzbr.videoeditingservice.model.entity.audio;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "server_audio")
public class ServerAudio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String url;

	@OneToMany(mappedBy = "serverAudioEntity")
	List<SelectedServerAudio> selectedServerAudioEntities;
}
