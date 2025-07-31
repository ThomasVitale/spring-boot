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

import org.testcontainers.containers.Container;

import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp.OtlpMetricsConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

/**
 * Factory for creating {@link OtlpMetricsConnectionDetails} for OpenTelemetry containers
 * using the {@code "otel/opentelemetry-collector-contrib"} image.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
class OpenTelemetryOtlpMetricsContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<Container<?>, OtlpMetricsConnectionDetails> {

	OpenTelemetryOtlpMetricsContainerConnectionDetailsFactory() {
		super("otel/opentelemetry-collector-contrib");
	}

	@Override
	protected OtlpMetricsConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<Container<?>> source) {
		return new OpenTelemetryOtlpMetricsContainerConnectionDetails(source);
	}

	private static final class OpenTelemetryOtlpMetricsContainerConnectionDetails
			extends ContainerConnectionDetails<Container<?>> implements OtlpMetricsConnectionDetails {

		private OpenTelemetryOtlpMetricsContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
			super(source);
		}

		@Override
		public String getUrl(Transport transport) {
			return switch (transport) {
				case HTTP -> "http://%s:%d%s".formatted(getContainer().getHost(),
						getContainer().getMappedPort(DEFAULT_HTTP_PORT), METRICS_PATH);
				case GRPC ->
					"http://%s:%d".formatted(getContainer().getHost(), getContainer().getMappedPort(DEFAULT_GRPC_PORT));
			};
		}

	}

}
