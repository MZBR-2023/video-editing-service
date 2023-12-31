package com.mzbr.videoeditingservice.model.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "temp_preview")
@ToString
public class TempPreview {
	@Id
	String id;

	String s3Url;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId")
	Member member;

}
