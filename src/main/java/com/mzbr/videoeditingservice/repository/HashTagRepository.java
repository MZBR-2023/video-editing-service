package com.mzbr.videoeditingservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.entity.HashTag;

@Repository
public interface HashTagRepository extends JpaRepository<HashTag,Long> {
	Optional<HashTag> findByName(String name);
}
