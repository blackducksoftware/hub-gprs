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

package com.blackduck.integration.scm;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.blackduck.integration.scm.ci.ConcourseConfig;
import com.blackduck.integration.scm.dao.PersistanceConfig;
import com.blackduck.integration.scm.fileinject.InjectServer;

@Configuration
@EnableTransactionManagement
public class ApplicationConfig {

	@Value("${debug.buildLogDirectory}")
	private String buildLogDirectory;

	@Value("${blackduck.hub.url}")
	private String hubUrl;

	@Value("${blackduck.hub.username}")
	private String hubUsername;

	@Value("${blackduck.hub.password}")
	private String hubPassword;
	
	@Value("${blackduck.hub-scm.static-location}")
	private String localStaticDirectory;
	
	@Value("${blackduck.hub-scm.hub-detect-version}")
	private String hubDetectVersion;

	@Inject
	private ConcourseConfig concourseConfiguration;

	@Inject
	private PersistanceConfig persistanceConfiguration;
	

	@Nonnull
	private Optional<String> getBuildLogDirectory() {
		return StringUtils.isNotBlank(buildLogDirectory) ? Optional.of(buildLogDirectory) : Optional.empty();
	}

	@Bean(initMethod = "startMonitoring")
	public BuildMonitor buildMonitor() {
		return new BuildMonitor(concourseConfiguration.concourseClient(), persistanceConfiguration.ciBuildDao(),
				getBuildLogDirectory());
	}

	@Bean
	public DeploymentService deploymentService() {
		return new DeploymentService(concourseConfiguration.concourseClient(), this.buildMonitor(), hubUrl, hubUsername,
				hubPassword, hubDetectVersion);
	}
	
	
	@Bean(initMethod="start")
	public InjectServer injectServer() {
		return new InjectServer(persistanceConfiguration.buildDao(), persistanceConfiguration.fileDao(), localStaticDirectory);
	}

}
