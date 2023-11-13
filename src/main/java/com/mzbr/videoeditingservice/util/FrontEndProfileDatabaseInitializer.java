package com.mzbr.videoeditingservice.util;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mzbr.videoeditingservice.model.entity.Member;
import com.mzbr.videoeditingservice.model.entity.Store;
import com.mzbr.videoeditingservice.repository.MemberRepository;
import com.mzbr.videoeditingservice.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Profile("frontend")
@Component
@RequiredArgsConstructor
public class FrontEndProfileDatabaseInitializer {

	private final MemberRepository memberRepository;
	private final StoreRepository storeRepository;
	@PostConstruct
	@Transactional
	public void init(){
		memberRepository.save(Member.builder().id(1).build());
		storeRepository.save(Store.builder().id(1L).build());
	}
}
