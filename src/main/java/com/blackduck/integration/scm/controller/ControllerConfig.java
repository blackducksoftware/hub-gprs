/*******************************************************************************
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

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
