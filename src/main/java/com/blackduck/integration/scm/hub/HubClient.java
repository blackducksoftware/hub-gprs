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

package com.blackduck.integration.scm.hub;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UrlConstants;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class HubClient {
	private static class HubUserInfo extends UserView{
		public String id;
		public String roleAssignmentsUrl;
	}
	
	private static class HubUserRole extends HubView{
		public String name;
		public String roleKey;
	}
	

	private static final Logger logger = LoggerFactory.getLogger(HubClient.class);

	/**
	 * Authenticates the provided user credentials in hub, returns the roles assigned to this user
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	public List<String> authenticate(String url, String username, String password) {
		final URL hubUrl;
		try {
			hubUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new HubCommunicationException("Illegal URL: " + url, e);
		}
		try {
			CredentialsRestConnection connection = new CredentialsRestConnection(new Slf4jIntLogger(logger), hubUrl, username, password, 22);

			HubResponseService responseService = new HubResponseService(connection);
			
			final HubUserInfo currentUser = responseService.getItem(hubUrl+"/api/v1/"+UrlConstants.SEGMENT_CURRENT_USER, HubUserInfo.class);
			if (Boolean.FALSE.equals(currentUser.active)) {
				throw new HubCommunicationException("User " + username + " is inactive.");
			}
			
			List<HubUserRole> userRoles = responseService.getAllItems(currentUser.roleAssignmentsUrl, HubUserRole.class);
			List<String> roles = userRoles.stream().map(role->role.roleKey).filter(StringUtils::isNotBlank).collect(Collectors.toList());
			return roles;

		} catch (IntegrationException e) {
			throw new HubCommunicationException("Error authenticating with hub", e);
		}
	}
}
