package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.Clip;

@Repository
public interface ClipRepository extends JpaRepository<Clip,Long> {
}
