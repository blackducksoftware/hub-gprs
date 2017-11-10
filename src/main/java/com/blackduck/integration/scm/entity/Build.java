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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(indexes = { @Index(columnList = "id,pipeline") })
public class Build {
	@Id
	@GeneratedValue(generator = "seq_build_id", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "seq_build_id", sequenceName = "seq_build_id", allocationSize = 1)
	private long id;

	@Enumerated
	@Column(nullable = false)
	private BuildType buildType;

	private String buildCommand;

	@Column(nullable = true)
	private String image;

	@Column(nullable = true)
	private String imageTag;

	@Column(nullable = true)
	private String projectName;

	@Column(nullable = true)
	private String versionName;
	
	@Column(nullable = true)
	private String hubDetectArguments;

	@ManyToOne(fetch = FetchType.EAGER)
	private Source source;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp with time zone not null default now()")
	private Date dateCreated;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp with time zone not null default now()")
	private Date dateUpdated;

	@Column(nullable = false, unique = true)
	private String pipeline;

	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "build", orphanRemoval = true, fetch=FetchType.EAGER)
	private List<FileInjection> fileInjections;
	
	@Transient
	private Properties properties = new Properties();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BuildType getBuildType() {
		return buildType;
	}

	public void setBuildType(BuildType buildType) {
		this.buildType = buildType;
	}

	public String getBuildCommand() {
		return buildCommand;
	}

	public void setBuildCommand(String buildCommand) {
		this.buildCommand = buildCommand;
	}

	public String getPipeline() {
		return pipeline;
	}

	public void setPipeline(String pipeline) {
		this.pipeline = pipeline;
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

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageTag() {
		return imageTag;
	}

	public void setImageTag(String imageTag) {
		this.imageTag = imageTag;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public List<FileInjection> getFileInjections() {
		return fileInjections;
	}

	public void setFileInjections(List<FileInjection> fileInjections) {
		this.fileInjections = fileInjections;
	}
	
	public String getHubDetectArguments() {
		return hubDetectArguments;
	}
	
	public void setHubDetectArguments(String hubDetectArguments) {
		this.hubDetectArguments = hubDetectArguments;
	}

	/**
	 * Returns a user-friendly name of this repository/build
	 * 
	 * @return
	 */
	@Transient
	public String getName() {
		String nameProperty = getSource().getType().getBuildIdentifierProperty();
		return getProperties().getProperty(nameProperty);
	}

	@Access(AccessType.PROPERTY)
	@Column(name = "properties", columnDefinition = "text")
	public String getPropertiesAsText() {
		try (StringWriter out = new StringWriter()) {
			this.properties.store(out, null);
			out.flush();
			return out.getBuffer().toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings("unused")
	public void setPropertiesAsText(String secretString) {
		this.properties = new Properties();
		try (StringReader reader = new StringReader(secretString)) {
			properties.load(reader);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "[" + getSource().getType().name() + "](" + getBuildType().getFriendlyName() + ") " + getName();
	}
}
