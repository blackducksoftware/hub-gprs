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

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.blackduck.integration.scm.DeploymentService;
import com.blackduck.integration.scm.dao.BuildDao;
import com.blackduck.integration.scm.dao.SourceDao;
import com.blackduck.integration.scm.entity.Build;
import com.blackduck.integration.scm.entity.ParamDefinition;
import com.blackduck.integration.scm.entity.ParamDefinition.ParamType;
import com.blackduck.integration.scm.entity.Source;
import com.blackduck.integration.scm.entity.SourceType;

@Controller
public class SourceController {

	private final SourceDao sourceDao;

	private final BuildDao buildDao;

	private final DeploymentService deploymentService;

	public SourceController(SourceDao sourceDao, BuildDao buildDao, DeploymentService deploymentService) {
		this.sourceDao = sourceDao;
		this.buildDao = buildDao;
		this.deploymentService = deploymentService;
	}

	@GetMapping("/newSource")
	public String newSource(Model model) {
		model.addAttribute("sourceTypes", SourceType.values());
		return "source";
	}

	@GetMapping("/sources")
	public String getSources(Model model) {
		List<Source> sources = sourceDao.list();
		model.addAttribute("sources", sources);
		return "sources";
	}

	@GetMapping("/sources/{id}")
	public String getSources(@PathVariable long id, Model model) {
		Source source = sourceDao.findById(id);
		model.addAttribute("source", source);
		return newSource(model);
	}

	@DeleteMapping("/sources/{id}")
	@Transactional
	public String deleteSource(@PathVariable long id, Model model) {
		if (buildDao.findAnyBySourceId(id).isPresent()) {
			model.addAttribute("message", "Unable to delete SCM. Please delete any repositories on it first.");
			return getSources(model);
		} else {
			sourceDao.delete(id);
			return getSources(model);
		}

	}

	@PostMapping("/sources")
	public String createSource(@RequestParam String name, @RequestParam String type,
			@RequestParam Map<String, String> allRequestParams, Model model) {
		Source source = new Source();
		source.setName(name);
		source.setType(SourceType.valueOf(StringUtils.upperCase(type)));
		setSourceTypeParamsFromRequest(source, allRequestParams);
		sourceDao.create(source);
		return getSources(model);
	}

	@PutMapping("/sources/{id}")
	public String updateSource(@PathVariable long id, @RequestParam String name,
			@RequestParam String type, @RequestParam Map<String, String> allRequestParams, Model model) {
		// Update the source
		Source source = sourceDao.findById(id);
		source.setName(name);
		source.setType(SourceType.valueOf(StringUtils.upperCase(type)));
		setSourceTypeParamsFromRequest(source, allRequestParams);
		sourceDao.update(source);
		
		// Since settings changed may effect existing builds, we should redeploy all the
		// existing builds on this source
		List<Build> toRedeploy = buildDao.findAllBySourceId(id);
		toRedeploy.forEach(build -> {
			deploymentService.undeploy(build.getPipeline());
			deploymentService.deploy(build);
		});
		return getSources(model);
	}

	private void setSourceTypeParamsFromRequest(Source source, Map<String, String> requestParams) {
		if (requestParams == null)
			return;
		String targetTypeName = source.getType().name();
		source.getProperties().clear();
		for (ParamDefinition definiton : source.getType().getSourceParameterDefinitions()) {
			String value = requestParams.get(targetTypeName + "_" + definiton.getName());
			if (ParamType.TRUE_FALSE == definiton.getType()) {
				value = Boolean.toString("on".equals(value));
			}
			source.getProperties().put(definiton.getName(), value);
		}
	}
}
