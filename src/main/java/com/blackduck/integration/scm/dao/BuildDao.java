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

package com.blackduck.integration.scm.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.scm.entity.Build;
import com.blackduck.integration.scm.entity.FileContent;
import com.blackduck.integration.scm.entity.FileInjection;

public class BuildDao {

	private final IBuildRepository buildRepository;

	private final IFileInjectionRepository fileInjectionRepository;

	public BuildDao(IBuildRepository buildRepository, IFileInjectionRepository fileInjectionRepository) {
		this.buildRepository = buildRepository;
		this.fileInjectionRepository = fileInjectionRepository;
	}

	public Stream<Build> getAll() {
		Iterable<Build> all = buildRepository.findAll();
		if (all == null) {
			return Stream.empty();
		} else {
			return StreamSupport.stream(all.spliterator(), false);
		}
	}

	public Build findById(long id) {
		return buildRepository.findOne(id);
	}

	/**
	 * Returns any existing build with specified source ID
	 * 
	 * @param sourceId
	 * @return
	 */
	public Optional<Build> findAnyBySourceId(long sourceId) {
		return findBySourceId(sourceId).findAny();
	}

	/**
	 * Finds all existing builds with the specified source ID
	 * 
	 * @param sourceId
	 * @return
	 */
	@Transactional
	public List<Build> findAllBySourceId(long sourceId) {
		return findBySourceId(sourceId).collect(Collectors.toList());
	}

	private Stream<Build> findBySourceId(long sourceId) {
		Stream<Build> result = buildRepository.findBySourceId(sourceId);
		return result == null ? Stream.empty() : result;
	}

	/**
	 * Persists a new build and returns the persisted instance. The original
	 * instance should be discarded.
	 * 
	 * @param build
	 * @return
	 */
	public Build create(Build build) {
		Date createDate = new Date();
		build.setDateCreated(createDate);
		build.setDateUpdated(createDate);
		return buildRepository.save(build);
	}

	/**
	 * Updates an existing build and returns the persisted instance.
	 * 
	 * @param build
	 * @return
	 */
	public Build update(Build build) {
		build.setDateUpdated(new Date());
		return buildRepository.save(build);
	}

	/**
	 * Deletes a build
	 */
	public void deleteById(long id) {
		buildRepository.delete(id);
	}

	/**
	 * Creates a new file injection
	 */
	public FileInjection newFileInjection(long buildId, FileContent fileContent, String targetPath) {
		Build build = findById(buildId);
		if (build == null) {
			throw new IllegalArgumentException("Non-existent build ID: " + buildId);
		}
		if (StringUtils.isBlank(targetPath)) {
			throw new IllegalArgumentException("Target path required.");
		}
		FileInjection injection = new FileInjection();
		injection.setFileContent(fileContent);
		injection.setTargetPath(targetPath);
		injection.setBuild(build);
		Date dateCreated = new Date();
		injection.setDateCreated(dateCreated);
		injection.setDateUpdated(dateCreated);
		return fileInjectionRepository.save(injection);
	}

}
