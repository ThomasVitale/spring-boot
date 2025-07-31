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

package org.springframework.boot.opentelemetry.autoconfigure.export;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.sdk.common.export.MemoryMode;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Compression;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;

/**
 * Configuration properties for OpenTelemetry exporters.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
@ConfigurationProperties(prefix = "management.opentelemetry.export")
public class OpenTelemetryExporterProperties {

	/**
	 * The type of OpenTelemetry exporter to use.
	 */
	private ExporterType type = ExporterType.OTLP;

	/**
	 * Common options for the OTLP exporters.
	 */
	private final Otlp otlp = new Otlp();

	/**
	 * Whether to reuse objects to reduce allocation or work with immutable data
	 * structures.
	 */
	private MemoryMode memoryMode = MemoryMode.REUSABLE_DATA;

	public ExporterType getType() {
		return this.type;
	}

	public void setType(ExporterType type) {
		this.type = type;
	}

	public Otlp getOtlp() {
		return this.otlp;
	}

	public MemoryMode getMemoryMode() {
		return this.memoryMode;
	}

	public void setMemoryMode(MemoryMode memoryMode) {
		this.memoryMode = memoryMode;
	}

	/**
	 * Configuration properties for exporting OpenTelemetry telemetry data using OTLP.
	 */
	public static class Otlp {

		/**
		 * The endpoint to which telemetry data will be sent.
		 */
		@Nullable private URI endpoint;

		/**
		 * The maximum waiting time for the exporter to send each telemetry batch.
		 */
		private Duration timeout = Duration.ofSeconds(10);

		/**
		 * The maximum waiting time for the exporter to establish a connection to the
		 * endpoint.
		 */
		private Duration connectTimeout = Duration.ofSeconds(10);

		/**
		 * Transport protocol to use for OTLP requests.
		 */
		private Transport transport = Transport.HTTP;

		/**
		 * Compression type to use for OTLP requests.
		 */
		private Compression compression = Compression.GZIP;

		/**
		 * Additional headers to include in each request to the endpoint.
		 */
		private Map<String, String> headers = new HashMap<>();

		/**
		 * Whether to generate metrics for the exporter.
		 */
		private boolean metrics = false;

		@Nullable public URI getEndpoint() {
			return this.endpoint;
		}

		public void setEndpoint(URI endpoint) {
			this.endpoint = endpoint;
		}

		public Duration getTimeout() {
			return this.timeout;
		}

		public void setTimeout(Duration timeout) {
			this.timeout = timeout;
		}

		public Duration getConnectTimeout() {
			return this.connectTimeout;
		}

		public void setConnectTimeout(Duration connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public Transport getTransport() {
			return this.transport;
		}

		public void setTransport(Transport transport) {
			this.transport = transport;
		}

		public Compression getCompression() {
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

		public boolean isMetrics() {
			return this.metrics;
		}

		public void setMetrics(boolean metrics) {
			this.metrics = metrics;
		}

	}

}
