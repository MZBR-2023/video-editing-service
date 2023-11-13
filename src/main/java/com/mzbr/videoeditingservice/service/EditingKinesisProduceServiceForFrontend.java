package com.mzbr.videoeditingservice.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

@Service
@Slf4j
@Profile("frontend")
public class EditingKinesisProduceServiceForFrontend extends EditingKinesisProduceService {
	public EditingKinesisProduceServiceForFrontend(KinesisClient kinesisClient) {
		super(kinesisClient);
	}

	@Override
	public PutRecordsResponse publishIdToKinesis(Long id) {

		return null;
	}
}
