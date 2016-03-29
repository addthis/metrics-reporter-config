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

package com.addthis.metrics3.reporter.config;

import java.util.ArrayList;
import java.util.List;

import com.addthis.metrics.reporter.config.AbstractStatsDReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricRegistry;
import com.readytalk.metrics.StatsDReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsDReporterConfig extends AbstractStatsDReporterConfig implements MetricsReporterConfigThree
{
    private static final String REPORTER_CLASS = "com.readytalk.metrics.StatsDReporter";
    private static final Logger log = LoggerFactory.getLogger(StatsDReporterConfig.class);

    private List<StatsDReporter> reporters = new ArrayList<StatsDReporter>();

    @Override
    public boolean enable(MetricRegistry registry)
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
                StatsDReporter reporter = StatsDReporter.forRegistry(registry)
                    .convertRatesTo(getRealRateunit())
                    .convertDurationsTo(getRealDurationunit())
                    .prefixedWith(getResolvedPrefix())
                    .filter(MetricFilterTransformer.generateFilter(getPredicate()))
                    .build(hostPort.getHost(), hostPort.getPort());
                reporter.start(getPeriod(), getRealTimeunit());
                reporters.add(reporter);
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

    @Override
    public void report() {
        for (StatsDReporter reporter : reporters)
        {
            reporter.report();
        }
    }

    public void stopForTests() {
        for (StatsDReporter reporter : reporters)
        {
            reporter.stop();
        }
    }
}
