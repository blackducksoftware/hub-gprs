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

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public enum BuildType {
	MVN("Maven", "maven", "3-jdk-8", "mvn package -D skipTests=true"),
	Gradle("Gradle", "openjdk", "8-jdk", "./gradlew build -x test"),
	None("Hub scan only", "openjdk", "8-jre","");

	private final String friendlyName;
	private final String image;
	private final String imageTag;
	private final String defaultBuildCommand;
	private final Set<ParamDefinition> params;

	private BuildType(String friendlyName, String image, String imageTag, String defaultBuildCommand, ParamDefinition...params) {
		this.friendlyName = friendlyName;
		this.params = ImmutableSet.copyOf(params);
		this.defaultBuildCommand = defaultBuildCommand;
		this.image = image;
		this.imageTag = imageTag;
	}

	public Set<ParamDefinition> getParams() {
		return params;
	}

	public String getDefaultBuildCommand() {
		return defaultBuildCommand;
	}

	public String getFriendlyName() {
		return friendlyName;
	}
	
	public String getImage() {
		return image;
	}
	
	public String getImageTag() {
		return imageTag;
	}
}
