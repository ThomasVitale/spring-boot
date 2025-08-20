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

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.opentelemetry.autoconfigure.export.OpenTelemetryExporterAutoConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.Transport;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.OpenTelemetryMetricsProperties;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.SdkMeterProviderBuilderCustomizer;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.OpenTelemetryMetricsExporterAutoConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.OpenTelemetryMetricsExporterProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OtlpMetricsExporterConfiguration}.
 *
 * @author Thomas Vitale
 */
class OtlpMetricsExporterConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(OpenTelemetryExporterAutoConfiguration.class,
				OpenTelemetryMetricsExporterAutoConfiguration.class))
		.withBean(CardinalityLimitSelector.class, CardinalityLimitSelector::defaultCardinalityLimitSelector)
		.withBean(OpenTelemetryMetricsProperties.class, OpenTelemetryMetricsProperties::new);

	@Test
	void otlpExporterConfigurationEnabledByDefault() {
		this.contextRunner.run(context -> {
			assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
			assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);
			assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
			assertThat(context).hasBean("histogramAggregation");

			// Verify default histogram aggregation
			OpenTelemetryMetricsExporterProperties properties = context
				.getBean(OpenTelemetryMetricsExporterProperties.class);
			assertThat(properties.getHistogramAggregation().name()).isEqualTo("EXPLICIT_BUCKET_HISTOGRAM");
		});
	}

	@Test
	void otlpExporterConfigurationEnabledWhenTypeIsOtlp() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.type=otlp").run(context -> {
			assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
			assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);
			assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
		});
	}

	@Test
	void otlpExporterConfigurationDisabledWhenTypeIsNotOtlp() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.type=none").run(context -> {
			assertThat(context).doesNotHaveBean(OtlpMetricsExporterConfiguration.class);
			assertThat(context).doesNotHaveBean(OtlpMetricsConnectionDetails.class);
			assertThat(context).doesNotHaveBean(OtlpHttpMetricExporter.class);
			assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
		});
	}

	@Test
	void httpProtobufExporterCreatedByDefault() {
		this.contextRunner.run(context -> {
			assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
		});
	}

	@Test
	void httpProtobufExporterCreatedWhenProtocolIsHttpProtobuf() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.otlp.transport=http")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
				assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
			});
	}

	@Test
	void grpcExporterCreatedWhenProtocolIsGrpc() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.otlp.transport=grpc")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpGrpcMetricExporter.class);
				assertThat(context).doesNotHaveBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void existingConnectionDetailsRespected() {
		this.contextRunner.withBean(OtlpMetricsConnectionDetails.class, () -> transport -> "http://test:4318")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpMetricsConnectionDetails.class);
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void aggregationTemporalityConfigurationRespected() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.aggregation-temporality=delta")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void compressionConfigurationRespected() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.otlp.compression=gzip")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void timeoutConfigurationRespected() {
		this.contextRunner
			.withPropertyValues("management.opentelemetry.metrics.export.otlp.timeout=5s",
					"management.opentelemetry.metrics.export.otlp.connect-timeout=2s")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void headersConfigurationRespected() {
		this.contextRunner
			.withPropertyValues("management.opentelemetry.metrics.export.otlp.headers.test=value",
					"management.opentelemetry.metrics.export.otlp.headers.common=shared")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void customEndpointConfigurationRespected() {
		this.contextRunner
			.withPropertyValues("management.opentelemetry.metrics.export.otlp.endpoint=http://custom:4318")
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
				OtlpMetricsConnectionDetails connectionDetails = context.getBean(OtlpMetricsConnectionDetails.class);
				assertThat(connectionDetails.getUrl(Transport.HTTP)).isEqualTo("http://custom:4318");
			});
	}

	@Test
	void existingHttpExporterRespected() {
		this.contextRunner.withBean(OtlpHttpMetricExporter.class, () -> OtlpHttpMetricExporter.builder().build())
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpHttpMetricExporter.class);
				assertThat(context).doesNotHaveBean(OtlpGrpcMetricExporter.class);
			});
	}

	@Test
	void existingGrpcExporterRespected() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.otlp.transport=grpc")
			.withBean(OtlpGrpcMetricExporter.class, () -> OtlpGrpcMetricExporter.builder().build())
			.run(context -> {
				assertThat(context).hasSingleBean(OtlpGrpcMetricExporter.class);
				assertThat(context).doesNotHaveBean(OtlpHttpMetricExporter.class);
			});
	}

	@Test
	void histogramAggregationConfigurationApplied() {
		this.contextRunner
			.withPropertyValues(
					"management.opentelemetry.metrics.export.histogram-aggregation=base2-exponential-bucket-histogram")
			.run(context -> {
				assertThat(context).getBeanNames(SdkMeterProviderBuilderCustomizer.class).hasSize(2);
				assertThat(context).hasBean("histogramAggregation");
				assertThat(context).hasBean("metricBuilderPlatformThreads");

				// Verify the histogram aggregation property is set correctly
				OpenTelemetryMetricsExporterProperties properties = context
					.getBean(OpenTelemetryMetricsExporterProperties.class);
				assertThat(properties.getHistogramAggregation().name()).isEqualTo("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM");
			});

		this.contextRunner
			.withPropertyValues(
					"management.opentelemetry.metrics.export.histogram-aggregation=explicit-bucket-histogram")
			.run(context -> {
				assertThat(context).getBeanNames(SdkMeterProviderBuilderCustomizer.class).hasSize(2);
				assertThat(context).hasBean("histogramAggregation");
				assertThat(context).hasBean("metricBuilderPlatformThreads");

				// Verify the histogram aggregation property is set correctly
				OpenTelemetryMetricsExporterProperties properties = context
					.getBean(OpenTelemetryMetricsExporterProperties.class);
				assertThat(properties.getHistogramAggregation().name()).isEqualTo("EXPLICIT_BUCKET_HISTOGRAM");
			});
	}

}
