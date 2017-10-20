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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
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

	private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

	private Logger logger = LoggerFactory.getLogger(BuildMonitor.class);

	private Set<Long> monitoredBuilds = Collections.synchronizedSet(new HashSet<>());

	@PostConstruct
	public void startMonitoring() {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				List<Long> activeBuilds = concourseClient.getActiveCiBuildIds();
				this.monitor(activeBuilds);
			} catch (Throwable t) {
				logger.error("Error monitoring CI builds.", t);
			}
		}, 15, 5, TimeUnit.SECONDS);
	}

	private void monitor(List<Long> ciBuildIds) {
		Set<Long> toMonitor = new HashSet<>(ciBuildIds);
		// Keep from monitoring what's already monitored
		synchronized (monitoredBuilds) {
			toMonitor.removeAll(monitoredBuilds);

			if (toMonitor.isEmpty())
				return;

			// Omit builds that are already completed
			Iterable<CiBuild> completedBuilds = ciBuildDao.findByIds(toMonitor);
			Set<Long> completedCiBuildIds = StreamSupport.stream(completedBuilds.spliterator(), false)
					.map(CiBuild::getId).collect(Collectors.toSet());
			toMonitor.removeAll(completedCiBuildIds);
			monitoredBuilds.addAll(toMonitor);
		}

		toMonitor.forEach(this::startObservingBuildEvents);

	}

	private void startObservingBuildEvents(long ciBuildId) {
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
			if (event.getData() != null) {
				try (Writer writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.APPEND)) {
					writer.write(event.getData().getPayload());
				} catch (Throwable t) {
					logger.error("Unable to write build log " + buildId, t);
				}
			}
		}
	}

}
