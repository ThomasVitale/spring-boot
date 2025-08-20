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

package org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp;

import java.util.Locale;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.internal.view.Base2ExponentialHistogramAggregation;
import io.opentelemetry.sdk.metrics.internal.view.ExplicitBucketHistogramAggregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.opentelemetry.autoconfigure.export.OpenTelemetryExporterProperties;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.SdkMeterProviderBuilderCustomizer;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.ConditionalOnOpenTelemetryMetricsExporter;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.OpenTelemetryMetricsExporterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Auto-configuration for exporting metrics via OTLP.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OtlpHttpMetricExporter.class)
@ConditionalOnOpenTelemetryMetricsExporter("otlp")
public final class OtlpMetricsExporterConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(OtlpMetricsExporterConfiguration.class);

	@Bean
	@ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
	PropertiesOtlpMetricsConnectionDetails otlpMetricsConnectionDetails(
			OpenTelemetryExporterProperties commonProperties, OpenTelemetryMetricsExporterProperties properties) {
		return new PropertiesOtlpMetricsConnectionDetails(commonProperties, properties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(OtlpMetricsConnectionDetails.class)
	@ConditionalOnProperty(prefix = "management.opentelemetry.metrics.export.otlp", name = "transport",
			havingValue = "http", matchIfMissing = true)
	OtlpHttpMetricExporter otlpHttpMetricExporter(OpenTelemetryExporterProperties commonProperties,
			OpenTelemetryMetricsExporterProperties properties, OtlpMetricsConnectionDetails connectionDetails,
			ObjectProvider<MeterProvider> meterProvider) {
		OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder()
			.setEndpoint(connectionDetails.getUrl(Transport.HTTP))
			.setTimeout(properties.getOtlp().getTimeout() != null ? properties.getOtlp().getTimeout()
					: commonProperties.getOtlp().getTimeout())
			.setConnectTimeout(properties.getOtlp().getConnectTimeout() != null
					? properties.getOtlp().getConnectTimeout() : commonProperties.getOtlp().getConnectTimeout())
			.setCompression(properties.getOtlp().getCompression() != null
					? properties.getOtlp().getCompression().name().toLowerCase(Locale.ROOT)
					: commonProperties.getOtlp().getCompression().name().toLowerCase(Locale.ROOT))
			.setAggregationTemporalitySelector(getAggregationTemporalitySelector(properties))
			.setMemoryMode(commonProperties.getMemoryMode());
		commonProperties.getOtlp().getHeaders().forEach(builder::addHeader);
		properties.getOtlp().getHeaders().forEach(builder::addHeader);
		if (properties.getOtlp().isMetrics() != null && Boolean.TRUE.equals(properties.getOtlp().isMetrics())
				|| properties.getOtlp().isMetrics() == null && commonProperties.getOtlp().isMetrics()) {
			meterProvider.ifAvailable(builder::setMeterProvider);
		}
		logger.info("Configuring OpenTelemetry HTTP/Protobuf metric exporter with endpoint: {}",
				connectionDetails.getUrl(Transport.HTTP));
		return builder.build();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(OtlpMetricsConnectionDetails.class)
	@ConditionalOnProperty(prefix = "management.opentelemetry.metrics.export.otlp", name = "transport",
			havingValue = "grpc")
	OtlpGrpcMetricExporter otlpGrpcMetricExporter(OpenTelemetryExporterProperties commonProperties,
			OpenTelemetryMetricsExporterProperties properties, OtlpMetricsConnectionDetails connectionDetails,
			ObjectProvider<MeterProvider> meterProvider) {
		OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder()
			.setEndpoint(connectionDetails.getUrl(Transport.GRPC))
			.setTimeout(properties.getOtlp().getTimeout() != null ? properties.getOtlp().getTimeout()
					: commonProperties.getOtlp().getTimeout())
			.setConnectTimeout(properties.getOtlp().getConnectTimeout() != null
					? properties.getOtlp().getConnectTimeout() : commonProperties.getOtlp().getConnectTimeout())
			.setCompression(properties.getOtlp().getCompression() != null
					? properties.getOtlp().getCompression().name().toLowerCase(Locale.ROOT)
					: commonProperties.getOtlp().getCompression().name().toLowerCase(Locale.ROOT))
			.setAggregationTemporalitySelector(getAggregationTemporalitySelector(properties))
			.setMemoryMode(commonProperties.getMemoryMode());
		commonProperties.getOtlp().getHeaders().forEach(builder::addHeader);
		properties.getOtlp().getHeaders().forEach(builder::addHeader);
		if (properties.getOtlp().isMetrics() != null && Boolean.TRUE.equals(properties.getOtlp().isMetrics())
				|| properties.getOtlp().isMetrics() == null && commonProperties.getOtlp().isMetrics()) {
			meterProvider.ifAvailable(builder::setMeterProvider);
		}
		logger.info("Configuring OpenTelemetry gRPC metric exporter with endpoint: {}",
				connectionDetails.getUrl(Transport.GRPC));
		return builder.build();
	}

	AggregationTemporalitySelector getAggregationTemporalitySelector(
			OpenTelemetryMetricsExporterProperties properties) {
		return switch (properties.getAggregationTemporality()) {
			case CUMULATIVE -> AggregationTemporalitySelector.alwaysCumulative();
			case DELTA -> AggregationTemporalitySelector.deltaPreferred();
			case LOW_MEMORY -> AggregationTemporalitySelector.lowMemory();
		};
	}

	@Bean
	SdkMeterProviderBuilderCustomizer histogramAggregation(OpenTelemetryMetricsExporterProperties properties) {
		return builder -> builder.registerView(InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
				View.builder().setAggregation(switch (properties.getHistogramAggregation()) {
					case BASE2_EXPONENTIAL_BUCKET_HISTOGRAM -> Base2ExponentialHistogramAggregation.getDefault();
					case EXPLICIT_BUCKET_HISTOGRAM -> ExplicitBucketHistogramAggregation.getDefault();
				}).build());
	}

	/**
	 * Implementation of {@link OtlpMetricsConnectionDetails} that uses properties to
	 * determine the OTLP endpoint.
	 */
	static class PropertiesOtlpMetricsConnectionDetails implements OtlpMetricsConnectionDetails {

		private final OpenTelemetryExporterProperties commonProperties;

		private final OpenTelemetryMetricsExporterProperties properties;

		public PropertiesOtlpMetricsConnectionDetails(OpenTelemetryExporterProperties commonProperties,
				OpenTelemetryMetricsExporterProperties properties) {
			this.commonProperties = commonProperties;
			this.properties = properties;
		}

		@Override
		public String getUrl(Transport transport) {
			var transportProperty = this.properties.getOtlp().getTransport() != null
					? this.properties.getOtlp().getTransport() : this.commonProperties.getOtlp().getTransport();
			Assert.state(transport == transportProperty, "Requested protocol %s doesn't match configured protocol %s"
				.formatted(transport, transportProperty));

			String url;
			if (this.properties.getOtlp().getEndpoint() != null) {
				url = this.properties.getOtlp().getEndpoint().toString();
			}
			else if (this.commonProperties.getOtlp().getEndpoint() != null) {
				url = transportProperty == Transport.HTTP
						? this.commonProperties.getOtlp().getEndpoint().resolve(METRICS_PATH).toString()
						: this.commonProperties.getOtlp().getEndpoint().toString();
			}
			else {
				url = transportProperty == Transport.HTTP ? DEFAULT_HTTP_PROTOBUF_ENDPOINT : DEFAULT_GRPC_ENDPOINT;
			}
			return url;
		}

	}

}
