package com.blackduck.integration.scm.ci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ResourceType {
	private String type;
	private String image;
	private String version;
	private boolean privileged;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public boolean isPrivileged() {
		return privileged;
	}
	
	public void setPrivileged(boolean privileged) {
		this.privileged = privileged;
	}
	
	
	
}
