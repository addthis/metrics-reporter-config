/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.addthis.metrics.reporter.config;

/**
 * A ConfiguredReporter is able to instantiate and configure a metrics reporter
 * (typically a subclass of
 * {@link com.yammer.metrics.reporting.AbstractReporter}.
 * <p>
 * When this project starts using Java 8, it would be a good idea to drop this
 * interface, and use {@link java.util.function.Supplier} instead, which is
 * functionally identical to this one. This eliminates the mandatory dependency
 * of the implementor on this project.
 * 
 * @author Tom van den Berge
 */
public interface ConfiguredReporter {

	/**
	 * Starts the configured metrics reporter.
	 * 
	 * @return {@code true} if the reporter was successfully started, otherwise
	 *         {@code false}.
	 */
	boolean enable();
}
