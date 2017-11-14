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

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.scm.dao.BuildDao;
import com.blackduck.integration.scm.dao.FileDao;
import com.blackduck.integration.scm.entity.FileInjection;
import com.blackduck.integration.scm.fileinject.InjectFileArchiveResource.FileInjectionMetadata;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
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

	static final String BUILD_FILE_ARCHIVE_PATH_PREFIX = "/buildFiles/";

	private final int injectPort = 13666;

	private final Undertow injectServer;

	private final BuildDao buildDao;

	private final FileDao fileDao;

	public InjectServer(BuildDao buildDao, FileDao fileDao, String localStaticFileLocation) {
		this.buildDao = buildDao;
		this.fileDao = fileDao;

		// Resource manager for static files inside the application jar:
		ResourceManager applicationStaticFileResourceManager = new ClassPathResourceManager(
				this.getClass().getClassLoader(), "build_resources/");

		// Resource manager for static files on local file system (e.g. JDK, hub-detect,
		// etc)
		ResourceManager fileSystemStaticFileResourceManager = new FileResourceManager(
				Paths.get(localStaticFileLocation).toFile());

		// Create a handler that chains them together, so that static files are searched in both places.
		ResourceHandler staticFileResourceHandler = new ResourceHandler(applicationStaticFileResourceManager,
				new ResourceHandler(fileSystemStaticFileResourceManager));

		// Create a handler to handle injected resources:
		ResourceHandler injectedFileResourceHandler = new ResourceHandler(
				(exchange, path) -> getInjectedFileResource(path));

		// Create a path handler to delegate to the above handlers:
		PathHandler pathHandler = new PathHandler(staticFileResourceHandler);
		pathHandler.addPrefixPath(BUILD_FILE_ARCHIVE_PATH_PREFIX, injectedFileResourceHandler);

		// Set up the server
		injectServer = Undertow.builder().addHttpListener(injectPort, "0.0.0.0").setHandler(pathHandler).build();

	}

	@Transactional
	private Resource getInjectedFileResource(String path) {
		long buildId = Long.parseLong(StringUtils.substringAfter(path, "/"));
		List<FileInjection> fileInjections = buildDao.findById(buildId).getFileInjections();
		List<FileInjectionMetadata> fileInjectionsMetadata = fileInjections.stream()
				.map(fileInjection -> new FileInjectionMetadata(fileInjection.getTargetPath(),
						fileInjection.getFileContent().getId(), fileInjection.getDateUpdated()))
				.collect(Collectors.toList());
		return new InjectFileArchiveResource(buildId, fileInjectionsMetadata, fileDao);
	}

	public void start() {
		injectServer.start();
	}

}
