package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.entity.TempVideo;

@Repository
public interface TempVideoRepository extends JpaRepository<TempVideo,Long> {
	TempVideo findByVideoName(String videoName);
}
