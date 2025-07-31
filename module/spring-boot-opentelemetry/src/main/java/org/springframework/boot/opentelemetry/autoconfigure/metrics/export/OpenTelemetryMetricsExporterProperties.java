package org.springframework.boot.opentelemetry.autoconfigure.metrics.export;

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.opentelemetry.autoconfigure.export.ExporterType;
import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.OtlpExporterConfig;

/**
 * Configuration properties for OpenTelemetry metrics.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
@ConfigurationProperties(prefix = "management.opentelemetry.metrics.export")
public class OpenTelemetryMetricsExporterProperties {

	/**
	 * The interval between two consecutive exports of metrics.
	 */
	private Duration interval = Duration.ofSeconds(60);

	/**
	 * The type of OpenTelemetry exporter to use for metrics.
	 */
	@Nullable private ExporterType type;

	/**
	 * The aggregation temporality to use for exporting metrics.
	 */
	private AggregationTemporalityStrategy aggregationTemporality = AggregationTemporalityStrategy.CUMULATIVE;

	/**
	 * The aggregation strategy to use for exporting histograms.
	 */
	private HistogramAggregationStrategy histogramAggregation = HistogramAggregationStrategy.EXPLICIT_BUCKET_HISTOGRAM;

	/**
	 * Options for the OTLP metrics exporter.
	 */
	@NestedConfigurationProperty
	private final OtlpExporterConfig otlp = new OtlpExporterConfig();

	public Duration getInterval() {
		return this.interval;
	}

	public void setInterval(Duration interval) {
		this.interval = interval;
	}

	@Nullable public ExporterType getType() {
		return this.type;
	}

	public void setType(@Nullable ExporterType type) {
		this.type = type;
	}

	public AggregationTemporalityStrategy getAggregationTemporality() {
		return this.aggregationTemporality;
	}

	public void setAggregationTemporality(AggregationTemporalityStrategy aggregationTemporality) {
		this.aggregationTemporality = aggregationTemporality;
	}

	public HistogramAggregationStrategy getHistogramAggregation() {
		return this.histogramAggregation;
	}

	public void setHistogramAggregation(HistogramAggregationStrategy histogramAggregation) {
		this.histogramAggregation = histogramAggregation;
	}

	public OtlpExporterConfig getOtlp() {
		return this.otlp;
	}

}
