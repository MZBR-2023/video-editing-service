package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.entity.TempPreview;

@Repository
public interface TempPreviewRepository extends JpaRepository<TempPreview,String> {

}
