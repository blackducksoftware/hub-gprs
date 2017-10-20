package com.blackduck.integration.scm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfiguration {
	
	@Value("${concourse.url}")
	private String concourseUrl;

	@Value("${concourse.username}")
	private String concourseUsername;

	@Value("${concourse.password}")
	private String concoursePassword;

	@Value("${debug.buildLogDirectory}")
	private String buildLogDirectory;
	
	
	public String getBuildLogDirectory() {
		return buildLogDirectory;
	}
	
	public String getConcoursePassword() {
		return concoursePassword;
	}
	
	public String getConcourseUrl() {
		return concourseUrl;
	}
	
	public String getConcourseUsername() {
		return concourseUsername;
	}
}
