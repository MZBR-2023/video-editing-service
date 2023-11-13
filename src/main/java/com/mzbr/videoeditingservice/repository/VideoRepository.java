package com.mzbr.videoeditingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

	@EntityGraph(value = "VideoEntity.all", type = EntityGraph.EntityGraphType.FETCH)
	Optional<Video> findById(Long id);

	Optional<Video> findByVideoUuid(String uuid);

	@EntityGraph(value = "VideoEntity.all", type = EntityGraph.EntityGraphType.FETCH)
	List<Video> findAll();


}
