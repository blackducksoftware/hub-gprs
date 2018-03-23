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
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.RoleAssignmentView;
import com.blackducksoftware.integration.hub.api.generated.view.RoleView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class HubClient {


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
			CredentialsRestConnection connection = new CredentialsRestConnection(new Slf4jIntLogger(logger), hubUrl, username, password, 22, new ProxyInfoBuilder().build());

			HubService hubService = new HubService(connection);
			
			
			UserView currentUser = hubService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
			if (Boolean.FALSE.equals(currentUser.active)) {
				throw new HubCommunicationException("User " + username + " is inactive.");
			}
			
			List<RoleAssignmentView> roleViews = hubService.getAllResponses(currentUser, UserView.ROLES_LINK_RESPONSE);
		
			List<String> roles = roleViews.stream()
					.map(role->role.name)
					.filter(StringUtils::isNotBlank)
					.map(StringUtils::lowerCase)
					.map(name -> StringUtils.replaceChars(name, ' ', '_'))
					.collect(Collectors.toList());
			return roles;

		} catch (IntegrationException e) {
			throw new HubCommunicationException("Error authenticating with hub", e);
		}
	}
}
