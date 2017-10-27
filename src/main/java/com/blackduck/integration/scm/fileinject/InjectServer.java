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

package com.blackduck.integration.scm.fileinject;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;

/**
 * Provides a simple HTTP server exposed to Concourse for feeding files into the
 * containers running builds (because no better mechanism for doing so exists).
 * The class creates and maintains a simple HTTP server. Because this server is
 * not set up for rate limiting or authorization, it should configured (through
 * orchestration or reverse proxy) to be accessible to Concourse workers only.
 * 
 * @author ybronshteyn
 */
public class InjectServer {
	private final int injectPort = 13666;

	private final Undertow injectServer;

	public InjectServer() {
		ResourceManager resourceManager = new ClassPathResourceManager(this.getClass().getClassLoader(),
				"build_resources/");
		injectServer = Undertow.builder().addHttpListener(injectPort, "0.0.0.0")
				.setHandler(new ResourceHandler(resourceManager)).build();
	}

	public void start() {
		injectServer.start();
	}

}
