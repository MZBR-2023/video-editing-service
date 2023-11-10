package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.TempCrop;

@Repository
public interface TempCropRepository extends JpaRepository<TempCrop,Long> {
}
