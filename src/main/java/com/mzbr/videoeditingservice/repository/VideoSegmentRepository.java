package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.entity.VideoSegment;

@Repository
public interface VideoSegmentRepository extends JpaRepository<VideoSegment, Long> {

}
