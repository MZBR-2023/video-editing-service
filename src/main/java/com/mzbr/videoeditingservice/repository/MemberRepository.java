package com.mzbr.videoeditingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.videoeditingservice.model.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member,Integer> {
}
