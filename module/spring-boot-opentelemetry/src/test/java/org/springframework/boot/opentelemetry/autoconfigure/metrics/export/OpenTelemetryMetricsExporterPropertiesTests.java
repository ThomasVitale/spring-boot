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

import org.junit.jupiter.api.Test;

import org.springframework.boot.opentelemetry.autoconfigure.export.ExporterType;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.OtlpExporterConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OpenTelemetryMetricsExporterProperties}.
 *
 * @author Thomas Vitale
 */
class OpenTelemetryMetricsExporterPropertiesTests {

	@Test
	void shouldCreateInstanceWithDefaultValues() {
		OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();

		assertThat(properties.getType()).isNull();
		assertThat(properties.getAggregationTemporality()).isEqualTo(AggregationTemporalityStrategy.CUMULATIVE);
		assertThat(properties.getHistogramAggregation())
			.isEqualTo(HistogramAggregationStrategy.EXPLICIT_BUCKET_HISTOGRAM);
		assertThat(properties.getOtlp()).isNotNull();
	}

	@Test
	void shouldUpdateType() {
		OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();

		properties.setType(ExporterType.NONE);

		assertThat(properties.getType()).isEqualTo(ExporterType.NONE);
	}

	@Test
	void shouldUpdateAggregationTemporality() {
		OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();

		properties.setAggregationTemporality(AggregationTemporalityStrategy.DELTA);

		assertThat(properties.getAggregationTemporality()).isEqualTo(AggregationTemporalityStrategy.DELTA);
	}

	@Test
	void shouldUpdateHistogramAggregation() {
		OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();

		properties.setHistogramAggregation(HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM);

		assertThat(properties.getHistogramAggregation())
			.isEqualTo(HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM);
	}

	@Test
	void shouldProvideAccessToOtlpConfig() {
		OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();

		assertThat(properties.getOtlp()).isNotNull().isInstanceOf(OtlpExporterConfig.class);
	}

}
