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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class CiBuild {

	@Id
	private long id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE })
	private Build build;

	@Column(nullable = false)
	private boolean violation;

	private boolean success;

	private boolean failure;

	public long getId() {
		return id;
	}

	public boolean isFailure() {
		return failure;
	}

	public boolean isSuccess() {
		return success;
	}

	public boolean isViolation() {
		return violation;
	}

	public Build getBuild() {
		return build;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	public void setViolation(boolean violation) {
		this.violation = violation;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setFailure(boolean failure) {
		this.failure = failure;
	}

}
