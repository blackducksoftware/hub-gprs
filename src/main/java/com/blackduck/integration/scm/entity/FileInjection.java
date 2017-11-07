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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(indexes = { @Index(name = "fileInjection_build_idx", columnList = "build_id", unique = false) })
public class FileInjection {

	private static final String sequenceName = "seq_file_injection_id";

	@Id
	@GeneratedValue(generator = sequenceName, strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = sequenceName, sequenceName = sequenceName, allocationSize = 1)
	private long id;

	@ManyToOne
	private Build build;

	// The eager fetch is just for the metadata fields. The actual contents of the
	// upload are fetched lazily inside FileContent.
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.DETACH)
	private FileContent fileContent;

	private String targetPath;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp with time zone not null default now()")
	private Date dateCreated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp with time zone not null default now()")
	private Date dateUpdated;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	public FileContent getFileContent() {
		return fileContent;
	}

	public void setFileContent(FileContent fileContent) {
		this.fileContent = fileContent;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

}
