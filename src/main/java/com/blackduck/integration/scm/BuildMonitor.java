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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.blackduck.integration.scm.ci.BuildEvent;
import com.blackduck.integration.scm.ci.ConcourseClient;

import rx.Observable;


@Component
public class BuildMonitor {

	@Inject
	private ConcourseClient concourseClient;

	private Set<Long> buildsInViolation = Collections.synchronizedSet(new HashSet<>());
	private Set<Long> monitoredBuilds = Collections.synchronizedSet(new HashSet<>());

	public void monitor(long buildId) {
		synchronized (monitoredBuilds) {
			if (monitoredBuilds.contains(buildId) || buildsInViolation.contains(buildId)) {
				return;
			}
			monitoredBuilds.add(buildId);
		}
		Observable<BuildEvent> eventObservable = concourseClient.observeBuildEvents(buildId)
				.doOnCompleted(()->monitoredBuilds.remove(buildId));
			
		eventObservable.forEach(received -> processEventUpdate(buildId, received));
	}

	private void processEventUpdate(long buildId,  BuildEvent event) {
		String payload = event.getData() != null ? event.getData().getPayload() : "";
		if (StringUtils.contains(payload, "Policy Status: IN_VIOLATION")) {
			buildsInViolation.add(buildId);
		}
	}
	
	public boolean isInViolation(long buildId) {
		return buildsInViolation.contains(buildId);
	}

}
