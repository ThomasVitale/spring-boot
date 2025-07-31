/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.opentelemetry.autoconfigure.export.otlp;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Configuration properties for exporting OpenTelemetry telemetry data using OTLP.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
public class OtlpExporterConfig {

	/**
	 * The endpoint to which telemetry data will be sent.
	 */
	@Nullable private URI endpoint;

	/**
	 * The maximum waiting time for the exporter to send each telemetry batch.
	 */
	@Nullable private Duration timeout;

	/**
	 * The maximum waiting time for the exporter to establish a connection to the
	 * endpoint.
	 */
	@Nullable private Duration connectTimeout;

	/**
	 * Transport protocol to use for OTLP requests.
	 */
	@Nullable private Transport transport;

	/**
	 * Compression type to use for OTLP requests.
	 */
	@Nullable private Compression compression;

	/**
	 * Additional headers to include in each request to the endpoint.
	 */
	private Map<String, String> headers = new HashMap<>();

	/**
	 * Whether to generate metrics for the exporter.
	 */
	@Nullable private Boolean metrics;

	@Nullable public URI getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(URI endpoint) {
		this.endpoint = endpoint;
	}

	@Nullable public Duration getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	@Nullable public Duration getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public @Nullable Transport getTransport() {
		return this.transport;
	}

	public void setTransport(@Nullable Transport transport) {
		this.transport = transport;
	}

	@Nullable public Compression getCompression() {
		return this.compression;
	}

	public void setCompression(Compression compression) {
		this.compression = compression;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	@Nullable public Boolean isMetrics() {
		return this.metrics;
	}

	public void setMetrics(Boolean metrics) {
		this.metrics = metrics;
	}

}
