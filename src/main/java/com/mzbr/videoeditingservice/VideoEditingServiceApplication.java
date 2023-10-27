package com.mzbr.videoeditingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VideoEditingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(VideoEditingServiceApplication.class, args);
	}
}
