package com.blackduck.integration.scm.ci;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConcourseConfiguration {
	
	@Value("${concourse.url}")
	private String concourseUrl;

	@Value("${concourse.username}")
	private String concourseUsername;

	@Value("${concourse.password}")
	private String concoursePassword;


	public ConcourseClient concourseClient() {
		return new ConcourseClient(concourseUrl, concourseUsername, concoursePassword);
	}
}
