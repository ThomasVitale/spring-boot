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

package org.springframework.boot.opentelemetry.autoconfigure.metrics.export;

import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.opentelemetry.autoconfigure.export.OpenTelemetryExporterAutoConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.OpenTelemetryMetricsProperties;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.SdkMeterProviderBuilderCustomizer;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp.OtlpMetricsExporterConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OpenTelemetryMetricsExporterAutoConfiguration}.
 *
 * @author Thomas Vitale
 */
class OpenTelemetryMetricsExporterAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(OpenTelemetryExporterAutoConfiguration.class,
				OpenTelemetryMetricsExporterAutoConfiguration.class))
		.withBean(CardinalityLimitSelector.class, CardinalityLimitSelector::defaultCardinalityLimitSelector)
		.withBean(OpenTelemetryMetricsProperties.class, OpenTelemetryMetricsProperties::new);

	@Test
	void configurationPropertiesEnabled() {
		this.contextRunner.run(context -> {
			assertThat(context).hasSingleBean(OpenTelemetryMetricsExporterProperties.class);
			assertThat(context).hasSingleBean(OpenTelemetryMetricsExporterAutoConfiguration.class);
		});
	}

	@Test
	void platformThreadsMetricBuilderCustomizerConfigurationApplied() {
		this.contextRunner
			.withPropertyValues("management.opentelemetry.metrics.export.interval=10s",
					"spring.threads.virtual.enabled=false")
			.run(context -> {
				assertThat(context).getBeanNames(SdkMeterProviderBuilderCustomizer.class).hasSize(2);
				assertThat(context).hasBean("histogramAggregation");
				assertThat(context).hasBean("metricBuilderPlatformThreads");
				assertThat(context).doesNotHaveBean("metricBuilderVirtualThreads");
			});
	}

	@Test
	void virtualThreadsMetricBuilderCustomizerConfigurationApplied() {
		this.contextRunner
			.withPropertyValues("management.opentelemetry.metrics.export.interval=10s",
					"spring.threads.virtual.enabled=true")
			.run(context -> {
				assertThat(context).getBeanNames(SdkMeterProviderBuilderCustomizer.class).hasSize(2);
				assertThat(context).hasBean("histogramAggregation");
				assertThat(context).hasBean("metricBuilderVirtualThreads");
				assertThat(context).doesNotHaveBean("metricBuilderPlatformThreads");
			});
	}

	@Test
	void otlpExporterConfigurationImportedWhenDefault() {
		this.contextRunner.run(context -> {
			assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
		});
	}

	@Test
	void otlpExporterConfigurationImportedWhenEnabled() {
		this.contextRunner.withPropertyValues("management.opentelemetry.metrics.export.type=otlp").run(context -> {
			assertThat(context).hasSingleBean(OtlpMetricsExporterConfiguration.class);
		});
	}

}
