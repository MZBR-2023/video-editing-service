package com.mzbr.videoeditingservice.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
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
@Table(name = "video")
@NamedEntityGraph(name = "VideoEntity.all"
	, attributeNodes = {
	@NamedAttributeNode(value = "clips", subgraph = "clips.subgraph"),
	@NamedAttributeNode(value = "userUploadAudioEntity"),
	@NamedAttributeNode(value = "selectedServerAudioEntity", subgraph = "selectedServerAudioEntity.subgraph")
},
	subgraphs = {
		@NamedSubgraph(name = "clips.subgraph", attributeNodes = @NamedAttributeNode("crop")),
		@NamedSubgraph(name = "selectedServerAudioEntity.subgraph", attributeNodes = @NamedAttributeNode("serverAudioEntity"))
	}
)
public class VideoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String videoUuid;
	String thumbnailUrl;

	@OneToMany(mappedBy = "videoEntity")
	List<Clip> clips;

	@OneToMany(mappedBy = "videoEntity")
	List<Subtitle> subtitles;

	@OneToOne(mappedBy = "videoEntity")
	UserUploadAudioEntity userUploadAudioEntity;

	@OneToOne(mappedBy = "videoEntity")
	SelectedServerAudioEntity selectedServerAudioEntity;



	public boolean hasAudio() {
		return userUploadAudioEntity != null || subtitles != null;
	}

	public Audio getAudio() {
		if (userUploadAudioEntity != null) {
			return userUploadAudioEntity;
		}
		return selectedServerAudioEntity;
	}

	public Integer getTotalDuration() {
		return clips.stream().mapToInt(Clip::getDurationTime)
			.sum();
	}

}
