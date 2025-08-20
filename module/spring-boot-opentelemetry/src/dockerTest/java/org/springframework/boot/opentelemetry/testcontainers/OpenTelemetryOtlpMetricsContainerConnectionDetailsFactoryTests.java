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

package org.springframework.boot.opentelemetry.testcontainers;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.export.OpenTelemetryExporterAutoConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp.OtlpMetricsConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testsupport.container.TestImage;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OpenTelemetryOtlpMetricsContainerConnectionDetailsFactory}.
 *
 * @author Thomas Vitale
 */
@SpringJUnitConfig
@Testcontainers(disabledWithoutDocker = true)
class OpenTelemetryOtlpMetricsContainerConnectionDetailsFactoryTests {

	@Container
	@ServiceConnection
	static final GenericContainer<?> lgtmContainer = TestImage.OPENTELEMETRY.genericContainer()
		.withExposedPorts(4317, 4318);

	@Autowired
	private OtlpMetricsConnectionDetails connectionDetails;

	@Test
	void shouldProvideConnectionDetailsForHttpProtobuf() {
		String url = this.connectionDetails.getUrl(Transport.HTTP);
		assertThat(url)
			.isEqualTo("http://" + lgtmContainer.getHost() + ":" + lgtmContainer.getMappedPort(4318) + "/v1/metrics");
	}

	@Test
	void shouldProvideConnectionDetailsForGrpc() {
		String url = this.connectionDetails.getUrl(Transport.GRPC);
		assertThat(url).isEqualTo("http://" + lgtmContainer.getHost() + ":" + lgtmContainer.getMappedPort(4317));
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration(OpenTelemetryExporterAutoConfiguration.class)
	static class TestConfiguration {

	}

}
