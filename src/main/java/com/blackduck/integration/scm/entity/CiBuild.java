package com.blackduck.integration.scm.entity;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class CiBuild {
	
	@Id
	private long id;
	
	@ManyToOne(fetch=FetchType.LAZY, cascade= {CascadeType.REMOVE})
	private Build build;
	
	@Column(nullable=false)
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
