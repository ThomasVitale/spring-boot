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

import org.springframework.boot.opentelemetry.autoconfigure.export.otlp.OtlpConnectionDetails;

/**
 * Connection details to establish a connection to an OTLP endpoint for metrics.
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
public interface OtlpMetricsConnectionDetails extends OtlpConnectionDetails {

	String METRICS_PATH = "/v1/metrics";

	int DEFAULT_GRPC_PORT = 4317;

	String DEFAULT_GRPC_ENDPOINT = "http://localhost:" + DEFAULT_GRPC_PORT;

	int DEFAULT_HTTP_PORT = 4318;

	String DEFAULT_HTTP_PROTOBUF_ENDPOINT = "http://localhost:" + DEFAULT_HTTP_PORT + METRICS_PATH;

}
