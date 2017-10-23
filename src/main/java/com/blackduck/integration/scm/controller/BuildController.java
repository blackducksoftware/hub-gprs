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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.blackduck.integration.scm.BuildStatus;
import com.blackduck.integration.scm.DeploymentService;
import com.blackduck.integration.scm.dao.BuildDao;
import com.blackduck.integration.scm.dao.SourceDao;
import com.blackduck.integration.scm.entity.Build;
import com.blackduck.integration.scm.entity.BuildType;
import com.blackduck.integration.scm.entity.ParamDefinition;
import com.blackduck.integration.scm.entity.Source;
import com.blackduck.integration.scm.entity.SourceType;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;

@Controller
public class BuildController {

	private static final Logger logger = LoggerFactory.getLogger(BuildController.class);

	@Inject
	private DeploymentService deploymentService;

	@Inject
	private BuildDao buildDao;

	@Inject
	private SourceDao sourceDao;

	/* Generates a standard pipeline name given all other fields of the build */
	private static String generatePipelineName(String name) {
		return name.replaceAll("/", "_") + UUID.randomUUID();
	}

	@GetMapping("/newBuild")
	public String editBuildView(Model model) {
		model.addAttribute("buildTypes", BuildType.values());
		model.addAttribute("sources", sourceDao.list());

		List<ParamDefinition> buildParams = Stream.of(SourceType.values())
				.flatMap(type -> type.getBuildParameterDefinitions().stream()).collect(Collectors.toList());
		model.addAttribute("allBuildParams", buildParams);
		return "build";
	}

	@PostMapping("/builds")
	public ResponseEntity<String> postDeployment(@RequestParam(required = true, name = "source_id") long sourceId,
			@RequestParam(required = true, name = "build_type") String buildTypeName,
			@RequestParam(required = true, name = "build_command") String buildCommand,
			@RequestParam(required = true, name = "build_image") String buildImage,
			@RequestParam(required = true, name = "build_image_tag") String buildImageTag,
			@RequestParam Map<String, String> allParameters) {
		Source source = sourceDao.findById(sourceId);
		if (source == null) {
			throw new IllegalArgumentException("Invalids source ID: " + sourceId);
		}

		Build build = new Build();
		BuildType buildType = BuildType.valueOf(buildTypeName);
		build.setSource(source);
		build.setBuildType(buildType);
		Map<String, String> buildProperties = new HashMap<>(extractParams(source, buildType, allParameters));
		build.getProperties().putAll(buildProperties);
		build.setBuildCommand(buildCommand);
		build.setPipeline(generatePipelineName(build.getName()));
		build.setImage(buildImage);
		build.setImageTag(buildImageTag);

		deploymentService.deploy(buildImage, buildImageTag, buildCommand, buildProperties, build.getPipeline());
		buildDao.create(build);
		return new ResponseEntity<>("{}", HttpStatus.CREATED);
	}

	@GetMapping("/builds")
	public String getBuilds(Model model) {
		List<Build> builds = buildDao.getAll().sorted(Ordering.usingToString()).collect(Collectors.toList());
		Map<String, BuildStatus> buildStatuses = builds.parallelStream().collect(Collectors.toMap(
				build -> Long.toString(build.getId()), build -> deploymentService.getStatus(build.getPipeline())));

		model.addAttribute("builds", builds);
		model.addAttribute("statuses", buildStatuses);
		return "builds";
	}

	@GetMapping("/builds/{id}")
	public String getBuildById(@PathVariable long id, Model model) {
		Build build = buildDao.findById(id);
		model.addAttribute("build", build);
		return editBuildView(model);
	}

	@GetMapping("/builds/{id}/status")
	@ResponseBody
	public ResponseEntity<String> getBuildStatus(@PathVariable long id) {
		Build build = buildDao.findById(id);
		if (build == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		BuildStatus status = deploymentService.getStatus(build.getPipeline());
		return new ResponseEntity<String>(status.toString(), HttpStatus.OK);
	}

	@PreAuthorize("ROLE_codescanner")
	@PutMapping("/builds/{id}")
	@Transactional
	public ResponseEntity<String> applyBuildEdits(@PathVariable long id,
			@RequestParam(required = true, name = "source_id") long sourceId,
			@RequestParam(required = true, name = "build_type") String buildTypeName,
			@RequestParam(required = true, name = "build_command") String buildCommand,
			@RequestParam(required = true, name = "build_image") String buildImage,
			@RequestParam(required = true, name = "build_image_tag") String buildImageTag,
			@RequestParam Map<String, String> allParameters) {

		Build build = buildDao.findById(id);
		String oldPipelineName = build.getPipeline();

		Source source = sourceDao.findById(sourceId);
		BuildType buildType = BuildType.valueOf(buildTypeName);
		build.setSource(source);
		build.setBuildType(buildType);
		Map<String, String> buildProperties = new HashMap<>(extractParams(source, buildType, allParameters));
		build.getProperties().putAll(buildProperties);
		build.setBuildCommand(buildCommand);
		build.setPipeline(generatePipelineName(build.getName()));
		build.setImage(buildImage);
		build.setImageTag(buildImageTag);

		// Redeploy to CI
		deploymentService.undeploy(oldPipelineName);
		deploymentService.deploy(buildImage, buildImageTag, buildCommand, buildProperties, build.getPipeline());
		buildDao.update(build);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@DeleteMapping("/builds/{id}")
	@Transactional
	public ResponseEntity<String> deleteById(@PathVariable long id) {
		Build build = buildDao.findById(id);
		try {
			deploymentService.undeploy(build.getPipeline());
		} catch (Throwable t) {
			logger.error("Unable to undeploy pipeline " + build.getPipeline() + " (build " + id + ")", t);
		}
		buildDao.deleteById(id);
		return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(HttpServletRequest req, Exception ex) {
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("hideNavbar", "true");
		mav.addObject("exception", ex);
		mav.addObject("error", ex.getClass().getSimpleName());
		mav.addObject("message", ex.getMessage());
		return mav;
	}

	/**
	 * Extracts and sanitizes request parameters appropriate for the provided
	 * source.
	 * 
	 * @param source
	 * @param values
	 * @return
	 */
	private Map<String, String> extractParams(Source source, BuildType buildType, Map<String, String> values) {

		Stream<ParamDefinition> allRequiredParamNames = Streams.concat(
				source.getType().getSourceParameterDefinitions().stream(),
				source.getType().getBuildParameterDefinitions().stream(), buildType.getParams().stream());

		return allRequiredParamNames
				// Get a value to bind from the build or source parameters
				.map(paramDef -> Pair.of(paramDef,
						ObjectUtils.firstNonNull(values.get(paramDef.getName()),
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
				.collect(Collectors.toMap(pair -> pair.getKey().getName(), Pair::getValue));
	}

	private String sanitizeParamValue(String param) {
		String[] toReplace = new String[] { "'", "\"" };
		String[] replacements = new String[toReplace.length];
		Arrays.fill(replacements, "_");
		return StringUtils.replaceEach(param, toReplace, replacements);
	}
}
