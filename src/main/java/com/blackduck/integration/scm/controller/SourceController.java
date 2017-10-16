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

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.blackduck.integration.scm.dao.BuildDao;
import com.blackduck.integration.scm.dao.SourceDao;
import com.blackduck.integration.scm.entity.ParamDefinition;
import com.blackduck.integration.scm.entity.Source;
import com.blackduck.integration.scm.entity.SourceType;
import com.blackduck.integration.scm.entity.ParamDefinition.ParamType;

@Controller
public class SourceController {

	@Inject
	private SourceDao sourceDao;
	
	@Inject
	private BuildDao buildDao;

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
	@ResponseBody
	@Transactional
	public ResponseEntity<String> deleteSource(@PathVariable long id, Model model) {
		if (buildDao.findBySourceId(id).findAny().isPresent()) {
			throw new IllegalStateException("Unable to delete SCM. Please delete any monitored repositories first.");
		}
		else {
			sourceDao.delete(id);
			return new ResponseEntity<>("{}", HttpStatus.OK);
		}
	
	}

	@PostMapping(path = "/sources", produces = "application/json")
	public ResponseEntity<String> createSource(@RequestParam String name, @RequestParam String type,
			@RequestParam Map<String, String> allRequestParams, Model model) {
		Source source = new Source();
		source.setName(name);
		source.setType(SourceType.valueOf(StringUtils.upperCase(type)));
		setSourceTypeParamsFromRequest(source, allRequestParams);
		sourceDao.create(source);
		return new ResponseEntity<>("{}", HttpStatus.CREATED);
	}

	@PutMapping(path = "/sources/{id}", produces = "application/json")
	public ResponseEntity<String> updateSource(@PathVariable long id, @RequestParam String name,
			@RequestParam String type, @RequestParam Map<String, String> allRequestParams, Model model) {
		Source source = sourceDao.findById(id);
		source.setName(name);
		source.setType(SourceType.valueOf(StringUtils.upperCase(type)));
		setSourceTypeParamsFromRequest(source, allRequestParams);
		sourceDao.update(source);
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}

	//TODO: Generalize property extraction logic
	private void setSourceTypeParamsFromRequest(Source source, Map<String, String> requestParams) {
		if (requestParams == null)
			return;
		String targetTypeName = source.getType().name();
		source.getProperties().clear();
		for (ParamDefinition definiton : source.getType().getSourceParameterDefinitions()) {
			String value = requestParams.get(targetTypeName+"_"+definiton.getName());
			if (ParamType.TRUE_FALSE == definiton.getType()) {
				value = Boolean.toString("on".equals(value));
			}
			source.getProperties().put(definiton.getName(), value);
		}
	}
}
