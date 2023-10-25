package com.mzbr.videoeditingservice.model;import javax.persistence.Entity;import javax.persistence.GeneratedValue;import javax.persistence.GenerationType;import javax.persistence.Id;import javax.persistence.JoinColumn;import javax.persistence.OneToOne;import javax.persistence.Table;import lombok.AllArgsConstructor;import lombok.Builder;import lombok.Getter;import lombok.NoArgsConstructor;@Getter@Entity@NoArgsConstructor@AllArgsConstructor@Builder@Table(name = "selected_server_video")public class SelectedServerAudioEntity extends Audio{	@Id	@GeneratedValue(strategy = GenerationType.IDENTITY)	Long id;	@OneToOne	@JoinColumn(name = "video_id")	VideoEntity videoEntity;	@OneToOne	@JoinColumn(name = "server_audio_id")	ServerAudioEntity serverAudioEntity;	@Override	String getUrl() {		return serverAudioEntity.getUrl();	}}