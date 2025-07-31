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
import org.junit.jupiter.api.Test;

import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Compression;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OpenTelemetryExporterProperties}.
 *
 * @author Thomas Vitale
 */
class OpenTelemetryExporterPropertiesTests {

	@Test
	void shouldCreateInstanceWithDefaultValues() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();

		assertThat(properties.getOtlp()).isNotNull();
		assertThat(properties.getOtlp().getEndpoint()).isNull();
		assertThat(properties.getOtlp().getTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(properties.getOtlp().getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(properties.getOtlp().getTransport()).isEqualTo(Transport.HTTP);
		assertThat(properties.getOtlp().getCompression()).isEqualTo(Compression.GZIP);
		assertThat(properties.getOtlp().getHeaders()).isNotNull().isEmpty();
		assertThat(properties.getOtlp().isMetrics()).isFalse();
		assertThat(properties.getMemoryMode()).isEqualTo(MemoryMode.REUSABLE_DATA);
	}

	@Test
	void shouldUpdateEndpoint() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();
		URI endpoint = URI.create("http://localhost:4318/v1/traces");

		properties.getOtlp().setEndpoint(endpoint);

		assertThat(properties.getOtlp().getEndpoint()).isEqualTo(endpoint);
	}

	@Test
	void shouldUpdateTimeout() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();
		Duration timeout = Duration.ofSeconds(30);

		properties.getOtlp().setTimeout(timeout);

		assertThat(properties.getOtlp().getTimeout()).isEqualTo(timeout);
	}

	@Test
	void shouldUpdateConnectTimeout() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();
		Duration connectTimeout = Duration.ofSeconds(20);

		properties.getOtlp().setConnectTimeout(connectTimeout);

		assertThat(properties.getOtlp().getConnectTimeout()).isEqualTo(connectTimeout);
	}

	@Test
	void shouldUpdateTransport() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();

		properties.getOtlp().setTransport(Transport.GRPC);

		assertThat(properties.getOtlp().getTransport()).isEqualTo(Transport.GRPC);
	}

	@Test
	void shouldUpdateCompression() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();

		properties.getOtlp().setCompression(Compression.NONE);

		assertThat(properties.getOtlp().getCompression()).isEqualTo(Compression.NONE);
	}

	@Test
	void shouldUpdateHeaders() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer token123");
		headers.put("Custom-Header", "value");

		properties.getOtlp().setHeaders(headers);

		assertThat(properties.getOtlp().getHeaders()).isNotNull()
			.hasSize(2)
			.containsEntry("Authorization", "Bearer token123")
			.containsEntry("Custom-Header", "value");
	}

	@Test
	void shouldUpdateMetrics() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();

		properties.getOtlp().setMetrics(true);

		assertThat(properties.getOtlp().isMetrics()).isTrue();
	}

	@Test
	void shouldUpdateMemoryMode() {
		OpenTelemetryExporterProperties properties = new OpenTelemetryExporterProperties();

		properties.setMemoryMode(MemoryMode.IMMUTABLE_DATA);

		assertThat(properties.getMemoryMode()).isEqualTo(MemoryMode.IMMUTABLE_DATA);
	}

}
