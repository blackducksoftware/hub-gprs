package com.blackduck.integration.scm.controller;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.blackduck.integration.scm.DeploymentService;
import com.blackduck.integration.scm.dao.BuildDao;
import com.blackduck.integration.scm.dao.SourceDao;

@Configuration
public class ControllerConfig {
	@Inject
	private SourceDao sourceDao;
	
	@Inject
	private BuildDao buildDao;
	
	@Inject
	private DeploymentService deploymentService;
	
	@Bean
	public SourceController sourceController() {
		return new SourceController(sourceDao, buildDao);
	}
	
	@Bean
	public BuildController buildController() {
		return new BuildController(sourceDao, buildDao, deploymentService);
	}
	
	@Bean
	public AuthController authController() {
		return new AuthController();
	}
}
