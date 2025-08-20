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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OnOpenTelemetryMetricsExporterCondition}.
 *
 * @author Thomas Vitale
 */
class OnOpenTelemetryMetricsExporterConditionTests {

	private final OnOpenTelemetryMetricsExporterCondition condition = new OnOpenTelemetryMetricsExporterCondition();

	private final MockEnvironment environment = new MockEnvironment();

	private final ConditionContext context = mock(ConditionContext.class);

	@Test
	void shouldMatchWhenMetricsExporterTypeMatches() {
		this.environment.setProperty("management.opentelemetry.metrics.export.type", "otlp");
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "otlp");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isTrue();
		assertThat(outcome.getMessage()).contains("management.opentelemetry.metrics.export.type is set to OTLP");
	}

	@Test
	void shouldMatchWhenGeneralExporterTypeMatches() {
		this.environment.setProperty("management.opentelemetry.export.type", "otlp");
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "otlp");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isTrue();
		assertThat(outcome.getMessage()).contains("management.opentelemetry.export.type is set to OTLP");
	}

	@Test
	void shouldMatchOtlpByDefault() {
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "otlp");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isTrue();
		assertThat(outcome.getMessage()).contains("management.opentelemetry.export.type is set to OTLP");
	}

	@Test
	void shouldNotMatchWhenExporterTypeDoesNotMatch() {
		this.environment.setProperty("management.opentelemetry.metrics.export.type", "otlp");
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "none");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isFalse();
		assertThat(outcome.getMessage())
			.contains("management.opentelemetry.metrics.export.type is set to OTLP, but requested none");
	}

	@Test
	void shouldNotMatchWhenExporterTypeIsEmpty() {
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isFalse();
		assertThat(outcome.getMessage()).contains("a valid exporter type is not specified");
	}

	@Test
	void shouldNotMatchWhenExporterTypeIsBlank() {
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "   ");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isFalse();
		assertThat(outcome.getMessage()).contains("a valid exporter type is not specified");
	}

	@Test
	void shouldNotMatchWhenExporterTypeIsNull() {
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", null);
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isFalse();
		assertThat(outcome.getMessage()).contains("a valid exporter type is not specified");
	}

	@Test
	void shouldNotMatchWhenAnnotationAttributesAreNull() {
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(null);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isFalse();
		assertThat(outcome.getMessage()).contains("a valid exporter type is not specified");
	}

	@Test
	void shouldPrioritizeMetricsExporterTypeOverGeneralExporterType() {
		this.environment.setProperty("management.opentelemetry.export.type", "none");
		this.environment.setProperty("management.opentelemetry.metrics.export.type", "otlp");
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "otlp");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isTrue();
		assertThat(outcome.getMessage()).contains("management.opentelemetry.metrics.export.type is set to OTLP");
	}

	@Test
	void shouldMatchCaseInsensitively() {
		this.environment.setProperty("management.opentelemetry.metrics.export.type", "otlp");
		when(this.context.getEnvironment()).thenReturn(this.environment);

		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("value", "Otlp");
		when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName()))
			.thenReturn(attributes);

		ConditionOutcome outcome = this.condition.getMatchOutcome(this.context, metadata);

		assertThat(outcome.isMatch()).isTrue();
		assertThat(outcome.getMessage()).contains("management.opentelemetry.metrics.export.type is set to OTLP");
	}

}
