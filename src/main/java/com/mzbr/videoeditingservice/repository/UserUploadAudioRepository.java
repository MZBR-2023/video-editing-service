package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mzbr.videoeditingservice.model.entity.audio.UserUploadAudio;

public interface UserUploadAudioRepository extends JpaRepository<UserUploadAudio, Long> {
}
