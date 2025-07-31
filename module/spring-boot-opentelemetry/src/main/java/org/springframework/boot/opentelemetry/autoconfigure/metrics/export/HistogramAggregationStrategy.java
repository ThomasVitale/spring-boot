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

import io.opentelemetry.sdk.metrics.internal.view.Base2ExponentialHistogramAggregation;
import io.opentelemetry.sdk.metrics.internal.view.ExplicitBucketHistogramAggregation;

/**
 * The strategy for the aggregation of histograms.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
public enum HistogramAggregationStrategy {

	/**
	 * Uses a base-2 exponential strategy to compress bucket boundaries and an integer
	 * scale parameter to manage the histogram resolution.
	 * @see Base2ExponentialHistogramAggregation#getDefault()
	 */
	BASE2_EXPONENTIAL_BUCKET_HISTOGRAM,

	/**
	 * Uses a pre-defined, fixed bucketing strategy to establish histogram bucket
	 * boundaries.
	 * @see ExplicitBucketHistogramAggregation#getDefault()
	 */
	EXPLICIT_BUCKET_HISTOGRAM

}
