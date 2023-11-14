package com.mzbr.videoeditingservice.model.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import com.mzbr.videoeditingservice.model.entity.audio.Audio;
import com.mzbr.videoeditingservice.model.entity.audio.SelectedServerAudio;
import com.mzbr.videoeditingservice.model.entity.audio.UserUploadAudio;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
	@NamedAttributeNode(value = "clips"),
	@NamedAttributeNode(value = "userUploadAudioEntity"),
	@NamedAttributeNode(value = "selectedServerAudioEntity", subgraph = "selectedServerAudioEntity.subgraph"),
	@NamedAttributeNode(value = "subtitles"),
},
	subgraphs = {
		@NamedSubgraph(name = "selectedServerAudioEntity.subgraph", attributeNodes = @NamedAttributeNode("serverAudioEntity"))
	}
)
public class Video {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String videoUuid;
	String thumbnailUrl;
	Integer segmentCount;

	@OneToMany(mappedBy = "videoEntity")
	Set<Clip> clips;

	@OneToMany(mappedBy = "videoEntity")
	Set<Subtitle> subtitles;

	@OneToOne(mappedBy = "videoEntity", fetch = FetchType.LAZY)
	VideoData videoData;

	@OneToOne(mappedBy = "videoEntity")
	UserUploadAudio userUploadAudioEntity;

	@OneToOne(mappedBy = "videoEntity")
	SelectedServerAudio selectedServerAudioEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId")
	Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="store_id")
	Store store;



	public boolean hasAudio() {
		return userUploadAudioEntity != null || subtitles != null;
	}

	public Audio getAudio() {
		if (userUploadAudioEntity != null) {
			return userUploadAudioEntity;
		}
		return selectedServerAudioEntity;
	}

	public void storeRegister(Store store) {
		this.store=store;
	}

	public void updateSegmentCount(Integer segmentCount) {
		this.segmentCount = segmentCount;
	}
}
