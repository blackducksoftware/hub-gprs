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

package com.blackduck.integration.scm.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${blackduck.hub.url}")
	private String hubUrl;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//Allow public access to static resources
		http.authorizeRequests().antMatchers(HttpMethod.GET, "/img/**", "/js/**","/css/**").permitAll()
		//Must have codescanner role to configure builds
		.antMatchers(HttpMethod.PUT, "/builds/**").hasAnyAuthority("codescanner")
		.antMatchers(HttpMethod.POST, "/builds").hasAnyAuthority("codescanner")
		.antMatchers(HttpMethod.DELETE, "/builds/**").hasAnyAuthority("codescanner")
		//Authenticate everything else
		.anyRequest().authenticated().and().httpBasic()
		//Add logout URL
		.and().formLogin().loginPage("/login").permitAll()
		.and().csrf().ignoringAntMatchers("/login");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(new HubAuthenticationProvider(hubUrl));
	}

}