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

package org.springframework.boot.opentelemetry.docker.compose;

import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp.OtlpMetricsConnectionDetails;

/**
 * {@link DockerComposeConnectionDetailsFactory} to create
 * {@link OtlpMetricsConnectionDetails} for an OTLP service.
 *
 * @author Thomas Vitale
 */
class OtlpMetricsDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<OtlpMetricsConnectionDetails> {

	private static final String[] OPENTELEMETRY_IMAGE_NAMES = { "otel/opentelemetry-collector-contrib",
			"grafana/otel-lgtm" };

	OtlpMetricsDockerComposeConnectionDetailsFactory() {
		super(OPENTELEMETRY_IMAGE_NAMES,
				"org.springframework.boot.opentelemetry.autoconfigure.metrics.export.OpenTelemetryMetricsExporterAutoConfiguration");
	}

	@Override
	protected OtlpMetricsConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new OtlpMetricsDockerComposeConnectionDetails(source.getRunningService());
	}

	private static final class OtlpMetricsDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements OtlpMetricsConnectionDetails {

		private final String host;

		private final int grpcPort;

		private final int httpPort;

		private OtlpMetricsDockerComposeConnectionDetails(RunningService source) {
			super(source);
			this.host = source.host();
			this.grpcPort = source.ports().get(DEFAULT_GRPC_PORT);
			this.httpPort = source.ports().get(DEFAULT_HTTP_PORT);
		}

		@Override
		public String getUrl(Transport transport) {
			return switch (transport) {
				case HTTP -> "http://%s:%d%s".formatted(this.host, this.httpPort, METRICS_PATH);
				case GRPC -> "http://%s:%d".formatted(this.host, this.grpcPort);
			};
		}

	}

}
