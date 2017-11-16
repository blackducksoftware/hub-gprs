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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import com.blackduck.integration.scm.entity.CiBuild;

/**
 * Persists information about outcomes of CI builds
 */
public class CiBuildDao {

	private final ICiBuildRepository ciBuildRepository;
	
	public CiBuildDao(ICiBuildRepository ciBuildRepository) {
		this.ciBuildRepository = Objects.requireNonNull(ciBuildRepository, "ci build repository must not be null");
	}

	public Optional<CiBuild> findById(long id) {
		return Optional.ofNullable(ciBuildRepository.findOne(id));
	}

	
	public Iterable<CiBuild> findByIds(Iterable<Long> ids) {
		Iterable<CiBuild> found = ciBuildRepository.findAll(ids);
		if (found ==null) found = Collections.emptyList();
		return found;
		
	}
	
	public void markViolation(long ciBuildId) {
		CiBuild ciBuild = findOrScaffold(ciBuildId);
		ciBuild.setViolation(true);
		ciBuildRepository.save(ciBuild);
	}

	public void markFailure(long ciBuildId) {
		CiBuild ciBuild = findOrScaffold(ciBuildId);
		ciBuild.setFailure(true);
		ciBuildRepository.save(ciBuild);
	}

	public void markSuccess(long ciBuildId) {
		CiBuild ciBuild = findOrScaffold(ciBuildId);
		ciBuild.setSuccess(true);
		ciBuildRepository.save(ciBuild);
	}

	/**
	 * Records that the build has completed without recording any status
	 * information. Does nothing if markViolation, markFailure, or markSuccess has
	 * already been called.
	 * 
	 * @param ciBuildId
	 */
	@Transactional
	public void recordCompletion(long ciBuildId) {
		CiBuild ciBuild = ciBuildRepository.findOne(ciBuildId);
		if (ciBuild == null) {
			ciBuild = new CiBuild();
			ciBuild.setId(ciBuildId);
			ciBuildRepository.save(ciBuild);
		}
	}

	/**
	 * Returns an existing ciBuild by ID or creates a new one but does not persist
	 * it
	 * 
	 * @param ciBuildId
	 * @return
	 */
	@Transactional
	private CiBuild findOrScaffold(long ciBuildId) {
		CiBuild ciBuild = ciBuildRepository.findOne(ciBuildId);
		if (ciBuild == null) {
			ciBuild = new CiBuild();
			ciBuild.setId(ciBuildId);
		}
		return ciBuild;
	}
}
