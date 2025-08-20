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

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.opentelemetry.autoconfigure.export.ExporterType;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Determines if a certain exporter type is enabled for OpenTelemetry metrics.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
class OnOpenTelemetryMetricsExporterCondition extends SpringBootCondition {

	private static final String GENERAL_EXPORTER_TYPE = "management.opentelemetry.export.type";

	private static final String METRICS_EXPORTER_TYPE = "management.opentelemetry.metrics.export.type";

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> attributes = metadata
			.getAnnotationAttributes(ConditionalOnOpenTelemetryMetricsExporter.class.getName());
		String requestedExporterType = attributes != null ? (String) attributes.get("value") : null;

		if (!StringUtils.hasText(requestedExporterType)) {
			return ConditionOutcome
				.noMatch(ConditionMessage.forCondition(ConditionalOnOpenTelemetryMetricsExporter.class)
					.because("a valid exporter type is not specified"));
		}

		String generalExporterTypeString = context.getEnvironment().getProperty(GENERAL_EXPORTER_TYPE, "otlp");
		ExporterType generalExporterType = StringUtils.hasText(generalExporterTypeString)
				? ExporterType.valueOf(generalExporterTypeString.toUpperCase()) : null;

		String metricsExporterTypeString = context.getEnvironment().getProperty(METRICS_EXPORTER_TYPE, String.class);
		ExporterType metricsExporterType = StringUtils.hasText(metricsExporterTypeString)
				? ExporterType.valueOf(metricsExporterTypeString.toUpperCase()) : null;

		if (metricsExporterType != null) {
			if (metricsExporterType.toString().equalsIgnoreCase(requestedExporterType)) {
				return ConditionOutcome
					.match(ConditionMessage.forCondition(ConditionalOnOpenTelemetryMetricsExporter.class)
						.because(METRICS_EXPORTER_TYPE + " is set to " + metricsExporterType));
			}
			else {
				return ConditionOutcome
					.noMatch(ConditionMessage.forCondition(ConditionalOnOpenTelemetryMetricsExporter.class)
						.because(METRICS_EXPORTER_TYPE + " is set to " + metricsExporterType + ", but requested "
								+ requestedExporterType));
			}
		}

		if (generalExporterType != null) {
			if (generalExporterType.toString().equalsIgnoreCase(requestedExporterType)) {
				return ConditionOutcome
					.match(ConditionMessage.forCondition(ConditionalOnOpenTelemetryMetricsExporter.class)
						.because(GENERAL_EXPORTER_TYPE + " is set to " + generalExporterType));
			}
			else {
				return ConditionOutcome
					.noMatch(ConditionMessage.forCondition(ConditionalOnOpenTelemetryMetricsExporter.class)
						.because(GENERAL_EXPORTER_TYPE + " is set to " + generalExporterType + ", but requested "
								+ requestedExporterType));
			}
		}

		return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnOpenTelemetryMetricsExporter.class)
			.because("exporter type not enabled: " + requestedExporterType));
	}

}
