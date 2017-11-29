package com.blackduck.integration.scm.ci;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true, value = { "status" })
public class Worker {
	public static class ResourceTypeList extends ArrayList<ResourceType>{
		private static final long serialVersionUID = 100181760431481561L;
	}
	
	public enum Status {
		STALLED, RUNNING, LANDING, LANDED
	}

	private Status status;
	private String name;
	private String version;

	@JsonProperty(value = "active_volumes")
	private int activeVolumes;

	@JsonProperty(value = "active_containers")
	private int activeContainers;

	@JsonProperty(value="resource_types")
	private ResourceTypeList resourceTypes;
	
	public int getActiveVolumes() {
		return activeVolumes;
	}

	public void setActiveVolumes(int activeVolumes) {
		this.activeVolumes = activeVolumes;
	}

	public int getActiveContainers() {
		return activeContainers;
	}
	
	public void setActiveContainers(int activeContainers) {
		this.activeContainers = activeContainers;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@JsonProperty("state")
	public String getStatusAsString() {
		return getStatus() == null ? "" : getStatus().toString().toLowerCase();
	}

	public void setStatusAsString(String status) {
		setStatus(Status.valueOf(StringUtils.upperCase(status)));
	}
	
	public ResourceTypeList getResourceTypes() {
		return resourceTypes;
	}
	
	public void setResourceTypes(ResourceTypeList resourceTypes) {
		this.resourceTypes = resourceTypes;
	}


}
