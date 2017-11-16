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
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.blackduck.integration.scm.entity.Build;


public class BuildDao {

	private final IBuildRepository buildRepository;
	
	public BuildDao(IBuildRepository buildRepository) {
		this.buildRepository = Objects.requireNonNull(buildRepository, "build repository must not be null");
	}

	public Stream<Build> getAll() {
		Iterable<Build>all = buildRepository.findAll();
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
	 * Streams builds by source Id
	 * @param sourceId
	 * @return
	 */
	public Stream<Build> findBySourceId(long sourceId){
		return buildRepository.findBySourceId(sourceId);
	}

	/**
	 * Persists a new build and returns the persisted instance. The original
	 * instance should be discarded.
	 * 
	 * @param build
	 * @return
	 */
	public Build create(Build build) {
		build.setCreatedOn(new Date());
		return buildRepository.save(build);
	}
	
	/**
	 * Updates an existing build and returns the persisted instance.
	 * @param build
	 * @return
	 */
	public Build update(Build build) {
		return buildRepository.save(build);
	}
	
	/**
	 * Deletes a build
	 */
	public void deleteById(long id) {
		buildRepository.delete(id);
	}
}
