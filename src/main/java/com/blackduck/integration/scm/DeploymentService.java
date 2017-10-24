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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.scm.ci.Build;
import com.blackduck.integration.scm.ci.CICommunicationException;
import com.blackduck.integration.scm.ci.ConcourseClient;

/**
 * Performs operations for deployment and tracking of builds, delegating CI-specific functions to concourseClient.
 * @author ybronshteyn
 *
 */
public class DeploymentService {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);

	private final ConcourseClient concourseClient;

	private final BuildMonitor buildMonitor;

	private final String hubUrl;
	
	private final String hubUsername;

	private final String hubPassword;
	
	public DeploymentService(ConcourseClient concourseClient, BuildMonitor buildMonitor, String hubUrl,
			String hubUsername, String hubPassword) {
		this.concourseClient = concourseClient;
		this.buildMonitor = buildMonitor;
		this.hubUrl = hubUrl;
		this.hubUsername = hubUsername;
		this.hubPassword = hubPassword;
	}

	private PipelineFactory pipelineFactory = new PipelineFactory();

	public void deploy(String buildImage, String buildImageTag, String buildCommand, Map<String, String> params,
			String pipelineName) {
		HashMap<String, String> fullParams = new HashMap<>(params);
		// Add hub info

		fullParams.put("hub_username", hubUsername);
		fullParams.put("hub_password", hubPassword);
		fullParams.put("hub_url", hubUrl);
		// Add build info
		fullParams.put("build_image", buildImage);
		fullParams.put("build_image_tag", buildImageTag);
		fullParams.put("build_command", buildCommand);

		String pipelineConfig = pipelineFactory.generatePipelineConfig(fullParams);
		concourseClient.addPipeline(pipelineName, pipelineConfig);
		concourseClient.unpausePipeline(pipelineName);
		concourseClient.forceCheck(pipelineName);
	}

	//TODO: Eliminate duplication with BuildMonitor
	public BuildStatus getStatus(String pipelineName) {
		try {
			Optional<Build> concourseBuild = concourseClient.getLatestBuild(pipelineName, "hub-detect");

			if (!concourseBuild.isPresent())
				return BuildStatus.NEVER_BUILT;
			long concourseBuildId = Long.valueOf(concourseBuild.get().getId());
			if (StringUtils.isBlank(concourseBuild.get().getEndTime())) {
				return BuildStatus.IN_PROGRESS;
			}
			if ("succeeded".equals(concourseBuild.get().getStatus())) {
				return BuildStatus.SUCCESS;
			} else if (buildMonitor.isInViolation(concourseBuildId)) {
				return BuildStatus.VIOLATION;
			} else {
				return BuildStatus.FAILED;
			}
		} catch (CICommunicationException e) {
			logger.error("Unable to communicate with CI", e);
			return BuildStatus.UNKNOWN;
		}
	}

	public void undeploy(String pipelineName) {
		concourseClient.destroyPipeline(pipelineName);
	}
	

}
