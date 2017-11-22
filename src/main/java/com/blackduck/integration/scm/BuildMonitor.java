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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.scm.ci.BuildEvent;
import com.blackduck.integration.scm.ci.ConcourseClient;
import com.blackduck.integration.scm.dao.CiBuildDao;
import com.blackduck.integration.scm.entity.Build;
import com.blackduck.integration.scm.entity.CiBuild;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import rx.Observable;

public class BuildMonitor {

	private final ConcourseClient concourseClient;

	private final CiBuildDao ciBuildDao;

	private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

	private final Optional<String> buildLogDirectory;

	private static final Logger logger = LoggerFactory.getLogger(BuildMonitor.class);

	private final Set<Long> monitoredBuilds = Collections.synchronizedSet(new HashSet<>());

	private Cache<Long, String> pipelineIdCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES)
			.build();

	/**
	 * 
	 * @param concourseClient
	 * @param ciBuildDao
	 * @param buildLogDirectory
	 * @param cleanup
	 */
	public BuildMonitor(ConcourseClient concourseClient, CiBuildDao ciBuildDao, Optional<String> buildLogDirectory) {
		this.ciBuildDao = ciBuildDao;
		this.concourseClient = concourseClient;
		this.buildLogDirectory = buildLogDirectory;
	}

	public void startMonitoring() {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				List<com.blackduck.integration.scm.ci.Build> activeCiBuilds = concourseClient.getActiveCiBuildIds();
				Set<Long> activeCiBuildIds = activeCiBuilds.stream()
						// Associate every build with its pipeline name for logging purposes.
						.peek(ciBuild -> pipelineIdCache.put(Long.parseLong(ciBuild.getId()), ciBuild.getPipelineName()))
						// Monitor for events
						.map(ciBuild -> Long.parseLong(ciBuild.getId())).collect(Collectors.toSet());
				monitor(activeCiBuildIds);

			} catch (Throwable t) {
				logger.error("Error monitoring CI builds.", t);
			}
		}, 15, 5, TimeUnit.SECONDS);
	}

	private void monitor(Set<Long> ciBuildIds) {
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
		if (buildLogDirectory.isPresent()) {
			String pipelineId = pipelineIdCache.getIfPresent(buildId);
			Path logFilePath = Paths.get(buildLogDirectory.get(), pipelineId + ".log");
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
					writer.write(StringUtils.defaultString(event.getData().getPayload()));
				} catch (Throwable t) {
					logger.error("Unable to write build log " + buildId, t);
				}
			}
		}
	}

}
