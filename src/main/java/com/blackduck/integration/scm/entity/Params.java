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

package com.blackduck.integration.scm.entity;

import static com.blackduck.integration.scm.entity.ParamDefinition.ParamType.*;

import com.blackduck.integration.scm.entity.ParamDefinition.ParamType;

public interface Params {

	
	public static final ParamDefinition REPOSITORY = new ParamDefinition("Repository");
	public static final ParamDefinition API_TOKEN = new ParamDefinition("api_token", "API Token",
			"Token required to access Github API", "", TEXT, true);
	public static final ParamDefinition API_ENDPOINT = new ParamDefinition("api_endpoint", "API Endpoint",
			"Location at which to access the Github API", "https://api.github.com", TEXT, true);
	public static final ParamDefinition USERNAME = new ParamDefinition("username", "Username",
			"required for private repositories", "", TEXT, false);
	public static final ParamDefinition PASSWORD = new ParamDefinition("password", "Password",
			"required for private repositories", "", ParamType.PASSWORD, false);
	public static final ParamDefinition SKIP_SSL_VERIFICATION = new ParamDefinition("no_ssl_verify",
			"Skip SSL verification", "", "false", TRUE_FALSE, true);
	public static final ParamDefinition PRIVATE_KEY = new ParamDefinition("private_key", "Private Key", "For private repositories, a private key with access to such repositories", "", ParamType.LARGE_TEXT, false);
	public static final ParamDefinition ADDRESS = new ParamDefinition("address", "Github hostname",
			"Do not include the scheme.", "www.github.com", TEXT,false);
}
