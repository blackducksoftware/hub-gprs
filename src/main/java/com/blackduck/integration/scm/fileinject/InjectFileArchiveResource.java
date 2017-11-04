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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.scm.dao.FileDao;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.util.ETag;
import io.undertow.util.MimeMappings;

public class InjectFileArchiveResource implements Resource {

	// A representation of DETACHED file injection metadata for a build
	static class FileInjectionMetadata {
		private String target;
		private long fileContentId;
		private Date dateUpdated;

		public FileInjectionMetadata(String target, long fileContentId, Date dateUpdated) {
			this.target = target;
			this.fileContentId = fileContentId;
			this.dateUpdated = dateUpdated;
		}

		public long getFileContentId() {
			return fileContentId;
		}

		public String getTargetPath() {
			return target;
		}

		public Date getDateUpdated() {
			return dateUpdated;
		}

	}

	private Logger logger = LoggerFactory.getLogger(InjectFileArchiveResource.class);

	private final Collection<FileInjectionMetadata> fileInjections;
	private final long buildId;
	private final FileDao fileDao;

	public InjectFileArchiveResource(long buildId, Collection<FileInjectionMetadata> fileInjections, FileDao fileDao) {
		this.fileInjections = fileInjections;
		this.buildId = buildId;
		this.fileDao = fileDao;
	}

	@Override
	public String getCacheKey() {
		// Identify all contents by IDs
		return fileInjections.stream().map(FileInjectionMetadata::getFileContentId).map(Object::toString)
				.collect(Collectors.joining(","));
	}

	@Override
	public Long getContentLength() {
		// Content length unknown in advance as contents will be zipped
		return null;
	}

	@Override
	public ETag getETag() {
		// Because the contents of a file under an ID cannot change without changing the
		// ID, the list of file IDs should be uniquely identifying
		ETag etag = new ETag(false, getCacheKey());
		return etag;
	}

	@Override
	public String getContentType(MimeMappings mimeMappings) {
		return "application/zip";
	}

	@Override
	public File getFile() {
		// Not a file
		return null;
	}

	@Override
	public Path getFilePath() {
		// Still not a file
		return null;
	}

	@Override
	public File getResourceManagerRoot() {
		// Did I mention this isn't a file?
		return null;
	}

	@Override
	public Path getResourceManagerRootPath() {
		// Nothing about this not being a file has changed from the previous method.
		return null;
	}

	@Override
	public boolean isDirectory() {
		// Nope
		return false;
	}

	@Override
	public List<Resource> list() {
		return null;
	}

	@Override
	public URL getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getLastModified() {
		// The resource is modified when the list of files injected into it is changed,
		// as the files themselves cannot be changed, only aded or removed.
		return fileInjections.stream().map(FileInjectionMetadata::getDateUpdated).max(Date::compareTo).orElse(null);
	}

	@Override
	public String getLastModifiedString() {
		Date lastModifiedDate = getLastModified();
		return lastModifiedDate == null ? null : lastModifiedDate.toString();
	}

	@Override
	public String getName() {
		return Long.toString(buildId);
	}

	@Override
	public String getPath() {
		return InjectServer.BUILD_FILE_ARCHIVE_PATH_PREFIX + Long.toString(buildId);
	}

	@Override
	@Transactional
	public void serve(Sender sender, HttpServerExchange exchange, IoCallback completionCallback) {
		boolean success = false;
		try (SenderOutputStream sos = new SenderOutputStream(sender); ZipOutputStream zos = new ZipOutputStream(sos)) {
			for (FileInjectionMetadata fileInjection : fileInjections) {
				ZipEntry entry = new ZipEntry(fileInjection.getTargetPath());
				zos.putNextEntry(entry);
				// Content must be lazy-loaded in a transaction, so it won't be available from
				// the injections.
				byte[] fileContent = fileDao.findById(fileInjection.getFileContentId()).getContent();
				zos.write(fileContent);
				zos.closeEntry();
			}
			sos.flush();
			success = true;
		} catch (IOException e) {
			logger.error("Unable to zip and send file contents", e);
			completionCallback.onException(exchange, sender, e);
		}
		if (success) completionCallback.onComplete(exchange, sender);

	}
}
