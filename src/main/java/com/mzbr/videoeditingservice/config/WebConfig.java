package com.mzbr.videoeditingservice.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mzbr.videoeditingservice.component.HeaderBasedMemberIdArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private HeaderBasedMemberIdArgumentResolver headerBasedMemberIdArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(headerBasedMemberIdArgumentResolver);
	}
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 모든 경로에 대해
			.allowedOrigins("http://localhost:3000") // 이 출처를 허용함
			.allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메소드
			.allowedHeaders("*") // 모든 헤더 허용
			.allowCredentials(true); // 쿠키/인증 정보와 함께 요청 허용
	}
}