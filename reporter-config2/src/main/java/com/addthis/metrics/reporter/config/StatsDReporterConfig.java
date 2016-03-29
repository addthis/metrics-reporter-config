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

import com.readytalk.metrics.StatsDConstructorHack;
import com.readytalk.metrics.StatsDReporter;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsDReporterConfig extends AbstractStatsDReporterConfig implements MetricsReporterConfigTwo
{
    private static final String REPORTER_CLASS = "com.readytalk.metrics.StatsDReporter";
    private static final Logger log = LoggerFactory.getLogger(StatsDReporterConfig.class);

    @Override
    public boolean enable()
    {
        if (!setup(REPORTER_CLASS))
        {
            return false;
        }
        boolean failures = false;
        for (HostPort hostPort : getFullHostList())
        {
            try
            {
                log.info("Enabling StatsDReporter to {}:{}",
                    new Object[] {hostPort.getHost(), hostPort.getPort()});
                StatsDReporter reporter = new StatsDReporter(
                    Metrics.defaultRegistry(),
                    getResolvedPrefix(),
                    MetricPredicateTransformer.generatePredicate(getPredicate()),
                    Clock.defaultClock(),
                    new StatsDConstructorHack(hostPort.getHost(), hostPort.getPort()));
                reporter.start(getPeriod(), getRealTimeunit());
            }
            catch (Exception e)
            {
                log.error("Failed to enable StatsDReporter to {}:{}",
                    new Object[] {hostPort.getHost(), hostPort.getPort()}, e);
                failures = true;
            }
        }
        return !failures;
    }
}
