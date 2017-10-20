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

package com.blackduck.integration.scm;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.scm.ci.BuildEvent;
import com.blackduck.integration.scm.ci.ConcourseClient;
import com.blackduck.integration.scm.dao.CiBuildDao;
import com.blackduck.integration.scm.entity.CiBuild;

import rx.Observable;

@Component
public class BuildMonitor {

	@Inject
	private ConcourseClient concourseClient;

	@Inject
	private ApplicationConfiguration applicationConfiguration;

	@Inject
	private CiBuildDao ciBuildDao;

	private Logger logger = LoggerFactory.getLogger(BuildMonitor.class);

	private Set<Long> monitoredBuilds = Collections.synchronizedSet(new HashSet<>());

	public void monitor(long ciBuildId) {
		synchronized (monitoredBuilds) {
			//Is the build already monitored?
			if (monitoredBuilds.contains(ciBuildId))
				return;
			// Has the build already finished?
			boolean completed = ciBuildDao.findById(ciBuildId)
					.map(ciBuild -> ciBuild.isFailure() || ciBuild.isSuccess() || ciBuild.isViolation()).orElse(false);
			if (completed)
				return;
			monitoredBuilds.add(ciBuildId);
		}
		Observable<BuildEvent> eventObservable = concourseClient.observeBuildEvents(ciBuildId)
				.doOnCompleted(() -> processBuildCompletion(ciBuildId));

		eventObservable.forEach(received -> processEventUpdate(ciBuildId, received));
	}

	private void processEventUpdate(long ciBuildId, BuildEvent event) {
		String payload = event.getData() != null ? event.getData().getPayload() : "";
		updateBuildLog(ciBuildId, event);
		if (StringUtils.contains(payload, "Policy Status: IN_VIOLATION")) {
			ciBuildDao.markViolation(ciBuildId);
		}
	}

	private void processBuildCompletion(long buildId) {
		ciBuildDao.recordCompletion(buildId);
		monitoredBuilds.remove(buildId);
	}

	public boolean isInViolation(long buildId) {
		return ciBuildDao.findById(buildId).map(CiBuild::isViolation).orElse(false);
	}

	private void updateBuildLog(long buildId, BuildEvent event) {
		if (StringUtils.isNotBlank(applicationConfiguration.getBuildLogDirectory())) {

			Path logFilePath = Paths.get(applicationConfiguration.getBuildLogDirectory(),
					Long.toString(buildId) + ".log");
			// Should we create the file?
			if (!Files.exists(logFilePath)) {
				// Double-check synchronously before creating a new file!
				synchronized (this) {
					if (!Files.exists(logFilePath)) {
						try {
							Files.createFile(logFilePath);
						} catch (IOException ioe) {
							logger.error("Unable to create file for build log: " + logFilePath.toString());
						}
					}
				}
			}

			try (Writer writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.APPEND)) {
				writer.write(event.getData().getPayload());
			} catch (Throwable t) {
				logger.error("Unable to write build log " + buildId, t);
			}
		}
	}

}
