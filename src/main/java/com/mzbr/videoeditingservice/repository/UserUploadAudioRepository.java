package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mzbr.videoeditingservice.model.UserUploadAudioEntity;

public interface UserUploadAudioRepository extends JpaRepository<UserUploadAudioEntity, Long> {
}
