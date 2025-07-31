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

import org.testcontainers.grafana.LgtmStackContainer;

import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp.OtlpMetricsConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

/**
 * Factory for creating {@link OtlpMetricsConnectionDetails} for Grafana LGTM containers
 * using the {@code "grafana/otel-lgtm"} image.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
class GrafanaOtlpMetricsContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<LgtmStackContainer, OtlpMetricsConnectionDetails> {

	GrafanaOtlpMetricsContainerConnectionDetailsFactory() {
		super(ANY_CONNECTION_NAME);
	}

	@Override
	protected OtlpMetricsConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<LgtmStackContainer> source) {
		return new GrafanaOtlpMetricsContainerConnectionDetails(source);
	}

	private static final class GrafanaOtlpMetricsContainerConnectionDetails
			extends ContainerConnectionDetails<LgtmStackContainer> implements OtlpMetricsConnectionDetails {

		private GrafanaOtlpMetricsContainerConnectionDetails(ContainerConnectionSource<LgtmStackContainer> source) {
			super(source);
		}

		@Override
		public String getUrl(Transport transport) {
			String url = switch (transport) {
				case HTTP -> getContainer().getOtlpHttpUrl();
				case GRPC -> getContainer().getOtlpGrpcUrl();
			};
			return transport == Transport.HTTP ? url + METRICS_PATH : url;
		}

	}

}
