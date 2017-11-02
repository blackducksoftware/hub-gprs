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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

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
import com.blackduck.integration.scm.dao.FileDao;
import com.blackduck.integration.scm.dao.SourceDao;
import com.blackduck.integration.scm.entity.Build;
import com.blackduck.integration.scm.entity.BuildType;
import com.blackduck.integration.scm.entity.FileContent;
import com.blackduck.integration.scm.entity.FileInjection;
import com.blackduck.integration.scm.entity.ParamDefinition;
import com.blackduck.integration.scm.entity.ParamDefinition.ParamType;
import com.blackduck.integration.scm.entity.Source;
import com.blackduck.integration.scm.entity.SourceType;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

@Controller
public class BuildController {

	private static final Logger logger = LoggerFactory.getLogger(BuildController.class);

	private final DeploymentService deploymentService;

	private final BuildDao buildDao;

	private final SourceDao sourceDao;

	private final FileDao fileDao;

	public BuildController(SourceDao sourceDao, BuildDao buildDao, FileDao fileDao,
			DeploymentService deploymentService) {
		this.sourceDao = sourceDao;
		this.buildDao = buildDao;
		this.deploymentService = deploymentService;
		this.fileDao = fileDao;

	}

	/* Generates a standard pipeline name given all other fields of the build */
	private static String generatePipelineName(String name) {
		return name.replaceAll("/", "_") + UUID.randomUUID();
	}

	@GetMapping("/newBuild")
	public String editBuildView(Model model) {
		model.addAttribute("buildTypes", BuildType.values());
		model.addAttribute("sources", sourceDao.list());
		model.addAttribute("injectionCandidates", fileDao.listFileContents());

		List<ParamDefinition> buildParams = Stream.of(SourceType.values())
				.flatMap(type -> type.getBuildParameterDefinitions().stream()).collect(Collectors.toList());
		model.addAttribute("allBuildParams", buildParams);
		return "build";
	}

	@PreAuthorize("ROLE_codescanner")
	@PostMapping("/builds")
	@ResponseBody
	public ResponseEntity<String> deployNewBuild(@RequestParam(required = true, name = "source_id") long sourceId,
			@RequestParam(required = true, name = "build_type") String buildTypeName,
			@RequestParam(required = true, name = "build_command") String buildCommand,
			@RequestParam(required = true, name = "build_image") String buildImage,
			@RequestParam(required = true, name = "build_image_tag") String buildImageTag,
			@RequestParam(required = false, name = "project_name") String projectName,
			@RequestParam(required = false, name = "version_name") String versionName,
			@RequestParam Map<String, String> allParameters) {
		Source source = sourceDao.findById(sourceId);
		if (source == null) {
			throw new IllegalArgumentException("Invalids source ID: " + sourceId);
		}

		Build build = new Build();
		BuildType buildType = BuildType.valueOf(buildTypeName);
		build.setSource(source);
		build.setBuildType(buildType);
		build.setProperties(extractParameters(buildType, source.getType(), allParameters));
		build.setBuildCommand(buildCommand);
		build.setPipeline(generatePipelineName(build.getName()));
		build.setProjectName(Strings.emptyToNull(projectName));
		build.setVersionName(Strings.emptyToNull(versionName));
		build.setImage(buildImage);
		build.setImageTag(buildImageTag);

		deploymentService.deploy(build);
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
		
		//Get the files that could potentially be injected into the build
		Stream<FileContent> fileContents = fileDao.listFileContents().stream();
		//...this means ones that are not already injected
		Set<Long> injectedFiles = build.getFileInjections().stream().map(FileInjection::getFileContent).map(FileContent::getId).collect(Collectors.toSet());
		List<FileContent> injectionCandidates = fileContents.filter(file -> !injectedFiles.contains(file.getId())).collect(Collectors.toList());
		
		model.addAttribute("injectionCandidates", injectionCandidates);
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
	@ResponseBody
	public ResponseEntity<String> applyBuildEdits(@PathVariable long id,
			@RequestParam(required = true, name = "source_id") long sourceId,
			@RequestParam(required = true, name = "build_type") String buildTypeName,
			@RequestParam(required = true, name = "build_command") String buildCommand,
			@RequestParam(required = true, name = "build_image") String buildImage,
			@RequestParam(required = true, name = "build_image_tag") String buildImageTag,
			@RequestParam(required = false, name = "project_name") String projectName,
			@RequestParam(required = false, name = "version_name") String versionName,
			@RequestParam Map<String, String> allParameters) {

		Build build = buildDao.findById(id);
		String oldPipelineName = build.getPipeline();

		Source source = sourceDao.findById(sourceId);
		BuildType buildType = BuildType.valueOf(buildTypeName);
		build.setSource(source);
		build.setBuildType(buildType);

		build.setProperties(extractParameters(buildType, source.getType(), allParameters));
		build.setBuildCommand(buildCommand);
		build.setPipeline(generatePipelineName(build.getName()));
		build.setImage(buildImage);
		build.setImageTag(buildImageTag);
		build.setProjectName(Strings.emptyToNull(projectName));
		build.setVersionName(Strings.emptyToNull(versionName));

		// Update the build in DB to stop monitoring it
		buildDao.update(build);
		deploymentService.undeploy(oldPipelineName);
		deploymentService.deploy(build);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ResponseBody
	@PostMapping("/builds/{id}/injections")
	public ResponseEntity<String> addFileInjection(@PathVariable long buildId, @RequestParam long fileId,
			@RequestParam String targetPath) {
		FileContent fileContent = fileDao.findById(fileId);
		buildDao.newFileInjection(buildId, fileContent, targetPath);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ResponseBody
	@DeleteMapping("/builds/{buildId}/injections/{injectionId}")
	public ResponseEntity<String> deleteInjectionById(@PathVariable long buildId, @PathVariable long injectionId) {
		Build build = buildDao.findById(buildId);
		Optional<FileInjection> injectionToDelete = build.getFileInjections().stream()
				.filter(inj -> injectionId == inj.getId()).findFirst();
		//Remove the injection from the build. Let JPA remove the orphan
		if (!injectionToDelete.isPresent())
			throw new IllegalArgumentException("File injection not found in build.");
		build.getFileInjections().remove(injectionToDelete.get());
		buildDao.update(build);
		return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);

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

	private Properties extractParameters(BuildType buildType, SourceType sourceType, Map<String, String> params) {
		Properties properties = new Properties();
		Iterable<ParamDefinition> expectedBuildParams = Iterables.concat(buildType.getParams(),
				sourceType.getBuildParameterDefinitions());
		for (ParamDefinition paramDef : expectedBuildParams) {
			String paramValue = params.get(paramDef.getName());
			if (ParamType.TRUE_FALSE == paramDef.getType()) {
				paramValue = Boolean.toString("on".equals(paramValue));
			}
			properties.put(paramDef.getName(), paramValue);
		}
		return properties;
	}
}
