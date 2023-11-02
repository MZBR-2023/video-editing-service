package com.mzbr.videoeditingservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.VideoEntity;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, Long> {

	@EntityGraph(value = "VideoEntity.all", type = EntityGraph.EntityGraphType.FETCH)
	Optional<VideoEntity> findById(Long id);
}
