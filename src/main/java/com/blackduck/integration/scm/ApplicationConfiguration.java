package com.blackduck.integration.scm;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.blackduck.integration.scm.ci.ConcourseConfiguration;
import com.blackduck.integration.scm.dao.PersistanceConfiguration;

@Configuration
public class ApplicationConfiguration {

	@Value("${debug.buildLogDirectory}")
	private String buildLogDirectory;
	
	@Inject
	private ConcourseConfiguration concourseConfiguration;
	
	@Inject
	private PersistanceConfiguration persistanceConfiguration;
	
	@Nonnull
	private Optional<String> getBuildLogDirectory(){
		return StringUtils.isNotBlank(buildLogDirectory) ? Optional.of(buildLogDirectory) : Optional.empty();
	}
	
	@Bean(initMethod="startMonitoring")
	private BuildMonitor buildMonitor() {
		return new BuildMonitor(concourseConfiguration.concourseClient(), persistanceConfiguration.ciBuildDao(), getBuildLogDirectory());
	}

}
