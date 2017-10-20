package com.blackduck.integration.scm.dao;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;

public class PersistanceConfiguration {
	
	@Inject
	private ISourceRepository sourceRepository;
	
	@Inject
	private IBuildRepository buildRepository;
	
	@Inject 
	private ICiBuildRepository ciBuildRepository;
	
	@Bean
	public SourceDao sourceDao() {
		return new SourceDao(sourceRepository);
	}
	
	@Bean
	public CiBuildDao ciBuildDao() {
		return new CiBuildDao(ciBuildRepository);
	}
	
	@Bean
	public BuildDao buildDao() {
		return new BuildDao(buildRepository);
	}
	
}
