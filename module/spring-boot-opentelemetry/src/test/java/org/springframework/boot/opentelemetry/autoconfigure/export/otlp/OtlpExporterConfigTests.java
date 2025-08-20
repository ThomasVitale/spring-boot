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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OtlpExporterConfig}.
 *
 * @author Thomas Vitale
 */
class OtlpExporterConfigTests {

	@Test
	void shouldCreateInstanceWithDefaultValues() {
		OtlpExporterConfig config = new OtlpExporterConfig();

		assertThat(config.getEndpoint()).isNull();
		assertThat(config.getTimeout()).isNull();
		assertThat(config.getConnectTimeout()).isNull();
		assertThat(config.getTransport()).isNull();
		assertThat(config.getCompression()).isNull();
		assertThat(config.getHeaders()).isEmpty();
		assertThat(config.isMetrics()).isNull();
	}

	@Test
	void shouldUpdateEndpoint() {
		OtlpExporterConfig config = new OtlpExporterConfig();
		URI endpoint = URI.create("http://localhost:4318/v1/traces");

		config.setEndpoint(endpoint);

		assertThat(config.getEndpoint()).isEqualTo(endpoint);
	}

	@Test
	void shouldUpdateTimeout() {
		OtlpExporterConfig config = new OtlpExporterConfig();
		Duration timeout = Duration.ofSeconds(30);

		config.setTimeout(timeout);

		assertThat(config.getTimeout()).isEqualTo(timeout);
	}

	@Test
	void shouldUpdateConnectTimeout() {
		OtlpExporterConfig config = new OtlpExporterConfig();
		Duration connectTimeout = Duration.ofSeconds(20);

		config.setConnectTimeout(connectTimeout);

		assertThat(config.getConnectTimeout()).isEqualTo(connectTimeout);
	}

	@Test
	void shouldUpdateProtocol() {
		OtlpExporterConfig config = new OtlpExporterConfig();

		config.setTransport(Transport.GRPC);

		assertThat(config.getTransport()).isEqualTo(Transport.GRPC);
	}

	@Test
	void shouldUpdateCompression() {
		OtlpExporterConfig config = new OtlpExporterConfig();

		config.setCompression(Compression.NONE);

		assertThat(config.getCompression()).isEqualTo(Compression.NONE);
	}

	@Test
	void shouldUpdateHeaders() {
		OtlpExporterConfig config = new OtlpExporterConfig();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer token123");
		headers.put("Custom-Header", "value");

		config.setHeaders(headers);

		assertThat(config.getHeaders()).isNotNull()
			.hasSize(2)
			.containsEntry("Authorization", "Bearer token123")
			.containsEntry("Custom-Header", "value");
	}

	@Test
	void shouldUpdateMetrics() {
		OtlpExporterConfig config = new OtlpExporterConfig();

		config.setMetrics(true);

		assertThat(config.isMetrics()).isTrue();
	}

}
