package com.mzbr.videoeditingservice.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "video")
@ToString
@EqualsAndHashCode(callSuper=false)
@NamedEntityGraph(name = "VideoEntity.all"
	, attributeNodes = {
	@NamedAttributeNode(value = "clips", subgraph = "clips.subgraph"),
	@NamedAttributeNode(value = "userUploadAudioEntity"),
	@NamedAttributeNode(value = "selectedServerAudioEntity", subgraph = "selectedServerAudioEntity.subgraph"),
	@NamedAttributeNode(value = "subtitles"),
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
	Set<Clip> clips;

	@OneToMany(mappedBy = "videoEntity")
	Set<Subtitle> subtitles;

	@OneToOne(mappedBy = "videoEntity")
	UserUploadAudioEntity userUploadAudioEntity;

	@OneToOne(mappedBy = "videoEntity")
	SelectedServerAudioEntity selectedServerAudioEntity;

	@ManyToOne
	@JoinColumn(name = "memberId")
	Member member;



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
