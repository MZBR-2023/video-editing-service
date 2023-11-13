package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.entity.Subtitle;

@Repository
public interface SubtitleRepository extends JpaRepository<Subtitle,Long> {
}
