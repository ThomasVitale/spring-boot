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

import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;

/**
 * The temporality of the aggregation of metrics.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
public enum AggregationTemporalityStrategy {

	/**
	 * All instruments will have cumulative temporality.
	 * @see AggregationTemporalitySelector#alwaysCumulative()
	 */
	CUMULATIVE,

	/**
	 * Counter (sync and async) and histograms will be delta, up-down counters (sync and
	 * async) will be cumulative.
	 * @see AggregationTemporalitySelector#deltaPreferred()
	 */
	DELTA,

	/**
	 * Sync counter and histograms will be delta, async counter and up-down counters (sync
	 * and async) will be cumulative.
	 * @see AggregationTemporalitySelector#lowMemory()
	 */
	LOW_MEMORY

}
