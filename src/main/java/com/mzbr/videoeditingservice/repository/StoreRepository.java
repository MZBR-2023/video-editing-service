package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store,Long> {
}
