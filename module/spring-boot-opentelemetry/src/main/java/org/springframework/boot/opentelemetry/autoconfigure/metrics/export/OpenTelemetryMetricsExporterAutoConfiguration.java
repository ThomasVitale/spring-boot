package org.springframework.boot.opentelemetry.autoconfigure.metrics.export;

import java.util.concurrent.Executors;

import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.SdkMeterProviderBuilderCustomizer;
import org.springframework.boot.opentelemetry.autoconfigure.metrics.export.otlp.OtlpMetricsExporterConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.util.NamedThreadFactory;
import org.springframework.boot.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.VirtualThreadTaskExecutor;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for exporting OpenTelemetry Metrics.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
@AutoConfiguration
@Import({ OtlpMetricsExporterConfiguration.class })
@EnableConfigurationProperties(OpenTelemetryMetricsExporterProperties.class)
public final class OpenTelemetryMetricsExporterAutoConfiguration {

	private static final String THREAD_NAME_PREFIX = "otel-metrics";

	@Bean
	@ConditionalOnThreading(Threading.PLATFORM)
	SdkMeterProviderBuilderCustomizer metricBuilderPlatformThreads(OpenTelemetryMetricsExporterProperties properties,
			CardinalityLimitSelector cardinalityLimitSelector, ObjectProvider<MetricExporter> metricExporters) {
		NamedThreadFactory threadFactory = new NamedThreadFactory(THREAD_NAME_PREFIX);
		return builder -> {
			metricExporters.orderedStream()
				.forEach(metricExporter -> builder.registerMetricReader(PeriodicMetricReader.builder(metricExporter)
					.setInterval(properties.getInterval())
					.setExecutor(Executors.newSingleThreadScheduledExecutor(threadFactory))
					.build(), cardinalityLimitSelector));
		};
	}

	@Bean
	@ConditionalOnThreading(Threading.VIRTUAL)
	SdkMeterProviderBuilderCustomizer metricBuilderVirtualThreads(OpenTelemetryMetricsExporterProperties properties,
			CardinalityLimitSelector cardinalityLimitSelector, ObjectProvider<MetricExporter> metricExporters) {
		VirtualThreadTaskExecutor taskExecutor = new VirtualThreadTaskExecutor(THREAD_NAME_PREFIX + "-");
		return builder -> {
			metricExporters.orderedStream()
				.forEach(metricExporter -> builder.registerMetricReader(PeriodicMetricReader.builder(metricExporter)
					.setInterval(properties.getInterval())
					.setExecutor(Executors.newSingleThreadScheduledExecutor(taskExecutor.getVirtualThreadFactory()))
					.build(), cardinalityLimitSelector));
		};
	}

}
