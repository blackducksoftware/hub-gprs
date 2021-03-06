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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistanceConfig {

	@Inject
	private ISourceRepository sourceRepository;

	@Inject
	private IBuildRepository buildRepository;

	@Inject
	private ICiBuildRepository ciBuildRepository;

	@Inject
	private IFileContentRepository fileContentRepository;
	
	@Inject
	private IFileInjectionRepository fileInjectionRepository;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public SourceDao sourceDao() {
		return new SourceDao(sourceRepository);
	}

	@Bean	
	public CiBuildDao ciBuildDao() {
		return new CiBuildDao(ciBuildRepository);
	}

	@Bean
	public BuildDao buildDao() {
		return new BuildDao(buildRepository, fileInjectionRepository, entityManager);
	}

	@Bean
	public FileDao fileDao() {
		return new FileDao(fileContentRepository);
	}

}
