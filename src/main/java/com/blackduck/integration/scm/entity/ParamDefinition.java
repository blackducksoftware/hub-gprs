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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Represents a field that may be required for one or more SCM or repository
 * types.
 * 
 * @author ybronshteyn
 *
 */
public class ParamDefinition {
	public enum ParamType{
		TEXT, LARGE_TEXT, PASSWORD, TRUE_FALSE;
	}
	
	private final String name;
	private final String friendlyName;
	private final String description;
	private final String defaultValue;
	private final boolean required;
	private final ParamType type;

	public ParamDefinition(String name) {
		this(name, name, "", "", ParamType.TEXT, true);
	}

	public ParamDefinition(String name, String friendlyName, String description, String defaultValue, ParamType type, boolean required) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.description = description;
		this.defaultValue = defaultValue;
		this.type = type;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public String getFriendlyName() {
		return friendlyName;
	}


	public String getDescription() {
		return description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isRequired() {
		return required;
	}
	
	public ParamType getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && this.toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getFriendlyName(), getDescription(), getDefaultValue(), getType(), isRequired());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ParamDefinition.class).add("name", getName())
				.add("friendlyName", getFriendlyName()).add("default", getDefaultValue())
				.add("description", description).add("type", getType())
				.add("required", isRequired()).toString();
	}
}
