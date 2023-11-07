package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mzbr.videoeditingservice.model.Crop;

public interface CropRepository extends JpaRepository<Crop,Long> {
}
