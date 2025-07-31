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

package org.springframework.boot.opentelemetry.autoconfigure.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that creates threads with a specified name prefix and a
 * sequential number suffix.
 *
 * <p>
 *
 * Adapted from the Micrometer project (io.micrometer.core.instrument.util.ThreadFactory).
 *
 * @author Thomas Vitale
 * @since 4.0.0
 */
public final class NamedThreadFactory implements ThreadFactory {

	private final AtomicInteger sequence = new AtomicInteger(1);

	private final String prefix;

	public NamedThreadFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r);
		int seq = this.sequence.getAndIncrement();
		thread.setName(this.prefix + (seq > 1 ? "-" + seq : ""));
		thread.setDaemon(true);
		return thread;
	}

}
