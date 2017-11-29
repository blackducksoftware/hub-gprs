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
package com.blackduck.integration.scm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.blackduck.integration.scm.BuildMonitor;

/**
 * Controller to display debugging/troubleshooting information
 * 
 * @author ybronshteyn
 *
 */
@Controller
public class DebugController {
	private final BuildMonitor buildMonitor;

	public DebugController(BuildMonitor buildMonitor) {
		this.buildMonitor = buildMonitor;
	}

	@GetMapping(path = "/debug")
	public String getDebugInfo(Model model) {
		model.addAttribute("debugInfo",
				buildMonitor.getDebugInfo());
		return "debug";
	}

}
