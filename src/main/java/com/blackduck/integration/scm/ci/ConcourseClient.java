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

package com.blackduck.integration.scm.ci;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.blackduck.integration.scm.ci.Worker.Status;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.UrlEscapers;

import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;

public class ConcourseClient {

	private static final Logger logger = LoggerFactory.getLogger(ConcourseClient.class);

	private static final String codeResourceName = "codebase-pr";

	private static final String buildTaskName = "hub-detect";

	private final String concourseUrl;
	private final String concourseUsername;
	private final String concoursePassword;
	private final String baseUrl;

	RestTemplate restTemplate = new RestTemplate();

	private HttpAsyncClient asyncClient;

	private enum ExpiringMapKey {
		INSTANCE;
	}

	// Concourse's token expires after 24 hours, so we'll make sure ours expires too
	// by keeping it in here:
	private PassiveExpiringMap<ExpiringMapKey, String> expiringTokenStore = new PassiveExpiringMap<ExpiringMapKey, String>(
			239 * 6 * 60 * 1000L);

	public ConcourseClient(String concourseUrl, String concourseUsername, String concoursePassword) {
		this.concourseUrl = concourseUrl;
		this.baseUrl = concourseUrl + "/api/v1/teams/main";
		this.concourseUsername = concourseUsername;
		this.concoursePassword = concoursePassword;

		restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + getToken());
				return execution.execute(request, body);
			}
		});
		try {
			ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
			PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
			CloseableHttpAsyncClient createdClient = HttpAsyncClientBuilder.create()
					.addInterceptorFirst(new HttpRequestInterceptor() {
						@Override
						public void process(org.apache.http.HttpRequest request, HttpContext context)
								throws HttpException, IOException {
							request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getToken());
						}
					}).setConnectionManager(cm).build();
			createdClient.start();
			asyncClient = createdClient;
		} catch (IOReactorException e) {
			logger.error("HTTP client setup error", e);
		}
	}

	/**
	 * Concourse has almost normal OAuth2. But, it uses GET instead of standard POST
	 * for basic authentication, which makes it unworkable with Spring's OAuth2
	 * client. So we'll have to do the token management ourselves.
	 * 
	 * @return
	 */
	private String getToken() {
		return expiringTokenStore.computeIfAbsent(ExpiringMapKey.INSTANCE, (key) -> {
			RestTemplate basicAuthTemplate = new RestTemplateBuilder()
					.basicAuthorization(concourseUsername, concoursePassword).defaultMessageConverters().build();

			ResponseEntity<AuthToken> response = basicAuthTemplate.getForEntity(baseUrl + "/auth/token",
					AuthToken.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new CICommunicationException(response.getStatusCode());
			}

			AuthToken token = response.getBody();
			if (!"Bearer".equals(token.getType())) {
				throw new CICommunicationException(
						"Unexpected token type. Expected: Bearer. Actual: " + token.getType());
			}
			return token.getValue();

		});
	}

	public List<Pipeline> getPipelineNames() {
		ResponseEntity<List<Pipeline>> response = restTemplate.exchange(baseUrl + "/pipelines", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Pipeline>>() {
				});
		if (response.getStatusCode().is2xxSuccessful())
			return response.getBody();
		else
			throw new CICommunicationException(response.getStatusCode());
	}

	/**
	 * Returns a list of Concourse build IDs and pipeline names that are currently
	 * in progress
	 * 
	 * @return
	 */
	public List<Build> getActiveCiBuildIds() {
		ResponseEntity<String> responseFromCi = restTemplate.getForEntity(concourseUrl + "/api/v1/builds?limit=100000",
				String.class);
		if (responseFromCi.getStatusCode().is2xxSuccessful()) {
			return deserializeBuilds(responseFromCi.getBody())
					// Active builds only
					.filter(build -> StringUtils.isBlank(build.getEndTime())).collect(Collectors.toList());
		} else
			throw new CICommunicationException(responseFromCi.getStatusCode());
	}

	/**
	 * Forces the CI to check for new code on a deployed pipeline
	 * 
	 * @param pipelineName
	 */
	public void forceCheck(String pipelineName) {
		String escapedPipelineName = escapePipelineName(pipelineName);
		String checkUri = baseUrl + "/pipelines/" + escapedPipelineName + "/resources/" + codeResourceName + "/check";
		String requestBody = "{}";
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(checkUri, requestBody, String.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				logger.error("Unable to check for code changes for pipeline " + pipelineName);
			}
		} catch (HttpServerErrorException e) {
			// Sometimes a POST to the check URI fails with a 500 and no explanation or
			// meaningful log output because Concourse.
			logger.warn("Unable to immediately check for code changes for pipeline: " + pipelineName + ": "
					+ e.getMessage() + "\n" + e.getResponseBodyAsString());
		}
	}

	public void forceBuild(String pipelineName) {
		String escapedPipelineName = escapePipelineName(pipelineName);
		String buildUri = baseUrl + "/pipelines/" + escapedPipelineName + "/jobs/" + buildTaskName + "/builds";
		String requestBody = "{}";
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(buildUri, requestBody, String.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				logger.error("Unable to check for code changes for pipeline " + pipelineName);
			}
		} catch (HttpServerErrorException e) {
			// Sometimes a POST to the check URI fails with a 500 and no explanation or
			// meaningful log output because Concourse.
			logger.warn("Unable to trigger build for pipeline: " + pipelineName + ": " + e.getMessage() + "\n"
					+ e.getResponseBodyAsString());
		}
	}

	public void addPipeline(String pipelineName, String pipelineConfig) {
		String escapedPipelineName = escapePipelineName(pipelineName);
		String putUrl = baseUrl + "/pipelines/" + escapedPipelineName + "/config";

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(2);
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList("application/json"));
		HttpEntity<String> entity = new HttpEntity<String>(pipelineConfig, headers);

		restTemplate.put(putUrl, entity);
	}

	public void unpausePipeline(String pipelineName) {
		String escapedPipelineName = escapePipelineName(pipelineName);
		String putUrl = baseUrl + "/pipelines/" + escapedPipelineName + "/unpause";
		restTemplate.put(putUrl, "");
	}

	public void destroyPipeline(String pipelineName) {
		String escapedPipelineName = escapePipelineName(pipelineName);
		String deleteUrl = baseUrl + "/pipelines/" + escapedPipelineName;
		try {
			restTemplate.delete(deleteUrl);
			pruneWorkers();
		} catch (HttpClientErrorException e) {
			// If 404, then we're ok - we didn't want it there anyway!
			if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
				logger.warn("Attmepting to delete pipeline not found on CI: " + escapedPipelineName);
			} else
				throw new CICommunicationException(e.getStatusCode());
		}
	}

	private String escapePipelineName(String pipelineName) {
		return UrlEscapers.urlFormParameterEscaper().escape(StringUtils.replaceChars(pipelineName, '/', '_'));
	}

	public Optional<Build> getLatestBuild(String pipelineName, String jobName) {
		String escapedPipelineName = escapePipelineName(pipelineName);
		String getUrl = baseUrl + "/pipelines/{pipeline}/jobs/{job}/builds?limit=1";

		// TODO: Currently Concourse returns content type of text/plain on build list
		// resources. A PR for this has been submitted, but for now, they must be
		// manually deserialized.
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.getForEntity(getUrl, String.class, escapedPipelineName, jobName);
		} catch (HttpClientErrorException e) {
			throw new CICommunicationException("Unable to obtain build list", e);
		}
		if (response.getStatusCode().is2xxSuccessful()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				Iterator<Build> buildIterator = mapper.readerFor(Build.class).readValues(response.getBody());
				if (!buildIterator.hasNext())
					return Optional.empty();
				else {
					Build build = buildIterator.next();
					return Optional.of(build);
				}
			} catch (IOException e) {
				throw new CICommunicationException("Unable to parse build list");
			}
		} else {
			throw new CICommunicationException(response.getStatusCode());
		}
	}

	private Stream<Worker> getWorkers() {
		String workersUrl = concourseUrl + "/api/v1/workers";
		String workerJson = restTemplate.getForObject(workersUrl, String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Iterator<Worker> worker = mapper.readerFor(Worker.class).readValues(workerJson);
			if (!worker.hasNext())
				return Stream.empty();
			else
				return StreamSupport.stream(Spliterators.spliteratorUnknownSize(worker, Spliterator.SIZED), false);
		} catch (IOException ioe) {
			throw new CICommunicationException("Unable to parse worker list", ioe);
		}

	}

	/**
	 * Prunes workers
	 */
	public void pruneWorkers() {
		Stream<String> pruneableWorkerNames = getWorkers()
				// Prune workers that are "STALLED" or "LANDED" (complete)
				.filter(worker -> Status.STALLED.equals(worker.getStatus()) || Status.LANDED.equals(worker.getStatus()))
				.map(Worker::getName);
		String pruneUrl = concourseUrl + "/api/v1/workers/{id}/prune";
		pruneableWorkerNames.forEach(workerName -> {
			try {
				restTemplate.put(pruneUrl, "{}", workerName);
			} catch (Throwable t) {
				logger.error("Error pruning workers.", t);
			}
		});
		logger.debug("Workers pruned.");
	}

	private Stream<Build> deserializeBuilds(String buildListJson) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Iterator<Build> buildIterator = mapper.readerFor(Build.class).readValues(buildListJson);
			if (!buildIterator.hasNext())
				return Stream.empty();
			else {
				return StreamSupport.stream(Spliterators.spliteratorUnknownSize(buildIterator, Spliterator.ORDERED),
						false);
			}
		} catch (IOException e) {
			throw new CICommunicationException("Unable to parse build list");
		}
	}

	public Observable<BuildEvent> observeBuildEvents(long buildId) {
		String uri = concourseUrl + "/api/v1/builds/" + Long.toString(buildId) + "/events";

		return ObservableHttp.createGet(uri, asyncClient).toObservable().flatMap(ObservableHttpResponse::getContent)
				.map(String::new).takeWhile(text -> !"event: end".equals(text))
				.filter(text -> StringUtils.startsWith(text, "data:"))
				.map(text -> StringUtils.substringAfter(text, "data:")).map(ConcourseClient::deserializeBuildEvent);
	}

	private static BuildEvent deserializeBuildEvent(String data) {
		try (JsonParser parser = new JsonFactory().createParser(data)) {
			return new ObjectMapper().readValue(data.getBytes(), BuildEvent.class);
		} catch (IOException e) {
			logger.error("Error parsing build event.", e);
			return new BuildEvent();
		}
	}
}
