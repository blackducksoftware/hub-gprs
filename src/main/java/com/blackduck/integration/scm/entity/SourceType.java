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

package com.blackduck.integration.scm.entity;

import static com.blackduck.integration.scm.entity.Params.*;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Defines a type of SCM.
 * @author ybronshteyn
 *
 */
public enum SourceType {
	GITHUB(ImmutableSet.of(API_TOKEN, API_ENDPOINT, PRIVATE_KEY, ADDRESS, SKIP_SSL_VERIFICATION), ImmutableSet.of(REPOSITORY), "Repository");

	private final Set<ParamDefinition> sourceParameterDefinitions;
	private final Set<ParamDefinition> repoParameterDefinitionss;
	private final String buildIdentifierProperty;
	

	/**
	 * Creates a new sourceType
	 * @param sourceParmeters Parameters to be supplied when configuring the source
	 * @param repoParameters Parameters to be supplied when configuring a repository in this source
	 * @param buildIdentifierProperty The property that represents a user-facing name for a build/repository in this source.
	 */
	private SourceType(Set<ParamDefinition> sourceParmeters, Set<ParamDefinition> repoParameters, String buildIdentifierProperty) {
		this.sourceParameterDefinitions = sourceParmeters;
		this.repoParameterDefinitionss = repoParameters;
		this.buildIdentifierProperty = buildIdentifierProperty;
	}

	public Set<ParamDefinition> getSourceParameterDefinitions() {
		return sourceParameterDefinitions;
	}

	public Set<ParamDefinition> getBuildParameterDefinitions() {
		return repoParameterDefinitionss;
	}
	
	public String getBuildIdentifierProperty() {
		return buildIdentifierProperty;
	}
	

}
