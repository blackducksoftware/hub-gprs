package com.blackduck.integration.scm.controller;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.blackduck.integration.scm.BuildMonitor;
import com.blackduck.integration.scm.DeploymentService;
import com.blackduck.integration.scm.dao.BuildDao;
import com.blackduck.integration.scm.dao.FileDao;
import com.blackduck.integration.scm.dao.SourceDao;

@Configuration
public class ControllerConfig {
	@Inject
	private SourceDao sourceDao;
	
	@Inject
	private BuildDao buildDao;
	
	@Inject
	private FileDao fileDao;
	
	@Inject
	private DeploymentService deploymentService;
	
	@Inject
	private BuildMonitor buildMonitor;
	
	@Bean
	public SourceController sourceController() {
		return new SourceController(sourceDao, buildDao, deploymentService);
	}
	
	@Bean
	public BuildController buildController() {
		return new BuildController(sourceDao, buildDao, fileDao, deploymentService);
	}
	
	@Bean
	public AuthController authController() {
		return new AuthController();
	}
	
	@Bean
	public FileController fileController() {
		return new FileController(fileDao);
	}
	
	@Bean
	public DebugController debugController() {
		return new DebugController(buildMonitor);
	}
}
