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
import java.util.Date;
import java.util.List;

import com.blackduck.integration.scm.entity.Source;

public class SourceDao {

	private final ISourceRepository sourceRepository;

	public SourceDao(ISourceRepository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}

	/**
	 * Returns a Source with the specified ID or null if not found
	 * 
	 * @param id
	 * @return
	 */
	public Source findById(long id) {
		return sourceRepository.findOne(id);
	}

	/**
	 * Creates a source with the data from the argument. Returns the newly-created
	 * source with the populated ID
	 * 
	 * @param source
	 * @return
	 */
	public Source create(Source source) {
		Date dateCreated = new Date();
		source.setDateCreated(dateCreated);
		source.setDateUpdated(dateCreated);
		return sourceRepository.save(source);
	}

	/**
	 * Delete a source by its ID
	 */
	public void delete(long sourceId) {
		sourceRepository.delete(sourceId);
	}

	/**
	 * Updates an existing source.
	 * 
	 * @param source
	 */
	// While this method presently has the same implementation as create(...), we
	// don't want to expose that fact.
	public void update(Source source) {
		source.setDateUpdated(new Date());
		sourceRepository.save(source);
	}

	public List<Source> list() {
		List<Source> sources = sourceRepository.listOrderByTypeAndName();
		return sources != null ? sources : Collections.emptyList();
	}

}
