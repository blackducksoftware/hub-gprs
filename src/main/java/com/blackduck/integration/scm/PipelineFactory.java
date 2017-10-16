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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Logic to prepare CI pipelines for deployment
 */
public class PipelineFactory {
	final Template template;

	public PipelineFactory() {
		// Read and populate the template
		String templateFileName = "pipeline_template.ftl";
		Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_26);
		try (InputStream templateStream = this.getClass().getClassLoader()
				.getResourceAsStream("pipeline_templates/" + templateFileName);
				InputStreamReader reader = new InputStreamReader(templateStream)) {
			this.template = new Template("pipelineTemplate", reader, freemarkerConfig);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to process pipeline template", e);
		}
	}

	public  String generatePipelineConfig (HashMap<String, String> values) {
		try(StringWriter out = new StringWriter()){
			this.template.process(values, out);
			return out.toString();
		}catch (IOException | TemplateException e) {
			throw new IllegalStateException("Unable to produce CI pipeline", e);
		}
	}
}
