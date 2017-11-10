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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.scm.ci.Build;
import com.blackduck.integration.scm.ci.CICommunicationException;
import com.blackduck.integration.scm.ci.ConcourseClient;
import com.blackduck.integration.scm.entity.ParamDefinition;
import com.blackduck.integration.scm.entity.Source;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;

/**
 * Performs operations for deployment and tracking of builds, delegating
 * CI-specific functions to concourseClient.
 * 
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

	public void deploy(com.blackduck.integration.scm.entity.Build build) {
		HashMap<String, String> fullParams = extractParams(build);
		
		// Add hub info
		fullParams.put("hub_username", hubUsername);
		fullParams.put("hub_password", hubPassword);
		fullParams.put("hub_url", hubUrl);
		// Add build info
		fullParams.put("build_image", build.getImage());
		fullParams.put("build_image_tag", build.getImageTag());
		fullParams.put("hub_detect_arguments", build.getHubDetectArguments());
		fullParams.put("build_command", Strings.nullToEmpty(build.getBuildCommand()));
		// Add project info
		if (StringUtils.isNotBlank(build.getProjectName()	))
			fullParams.put("project_name", build.getProjectName());
		if (StringUtils.isNotBlank(build.getVersionName())) {
			fullParams.put("version_name", build.getVersionName());
		}

		String pipelineConfig = pipelineFactory.generatePipelineConfig(build.getId(), fullParams);
		concourseClient.pruneWorkers();
		concourseClient.addPipeline(build.getPipeline(), pipelineConfig);
		concourseClient.unpausePipeline(build.getPipeline());
		concourseClient.forceCheck(build.getPipeline());
	}
	
	/**
	 * Collects, and sanitizes all build and source parameters for deployment. 
	 * 
	 * @param source
	 * @param values
	 * @return A mutable HashMap of sanitized parameters (for subsequent modification)
	 */
	private HashMap<String, String> extractParams(com.blackduck.integration.scm.entity.Build  build) {
		Source source = build.getSource();
		
		Stream<ParamDefinition> allRequiredParamNames = Streams.concat(
				source.getType().getSourceParameterDefinitions().stream(),
				source.getType().getBuildParameterDefinitions().stream(),
				build.getBuildType().getParams().stream());
		
		HashMap<String, String> result = new HashMap<>(build.getProperties().size()+source.getProperties().size());

		 allRequiredParamNames
				// Get a value to bind from the build or source parameters
				.map(paramDef -> Pair.of(paramDef,
						ObjectUtils.firstNonNull((String)build.getProperties().get(paramDef.getName()),
								(String) source.getProperties().get(paramDef.getName()))))
				// If a required value is missing, error out!
				.peek(pair -> {
					if (pair.getKey().isRequired() && StringUtils.isBlank(pair.getValue())) {
						throw new IllegalArgumentException("Missing value: " + pair.getKey().getFriendlyName());
					}
				})
				// Sanitize values
				.map(pair -> Pair.of(pair.getKey(), sanitizeParamValue(pair.getValue())))
				// Map'em!
				.forEach(pair -> result.put(pair.getKey().getName(), pair.getValue()));
		 return result;
	}
	

	private String sanitizeParamValue(String param) {
		String[] toReplace = new String[] { "'", "\"" };
		String[] replacements = new String[toReplace.length];
		Arrays.fill(replacements, "_");
		return StringUtils.replaceEach(param, toReplace, replacements);
	}
	

	// TODO: Eliminate duplication with BuildMonitor
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

	public void triggerBuild(String pipeline) {
		concourseClient.pruneWorkers();
		concourseClient.forceBuild(pipeline);
	}
	
	public void undeploy(String pipelineName) {
		concourseClient.destroyPipeline(pipelineName);
	}

}
