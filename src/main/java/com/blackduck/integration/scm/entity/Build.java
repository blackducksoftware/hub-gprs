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
import java.util.Properties;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
public class Build {
	@Id
	@GeneratedValue(generator = "seq_build_id", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "seq_build_id", sequenceName = "seq_build_id", allocationSize = 1)
	private long id;

	@Enumerated
	@Column(nullable=false)
	private BuildType buildType;

	private String buildCommand;
	
	@Column(nullable=false)
	private String image;
	
	@Column(nullable=false)
	private String imageTag;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Source source;

	@Temporal(TemporalType.TIMESTAMP)
	Date createdOn;

	@Nullable
	private String pipeline;

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

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
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

	@Transient
	private Properties properties = new Properties();

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties secrets) {
		this.properties = secrets;
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
	@Column(name = "properties", columnDefinition="text")
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
