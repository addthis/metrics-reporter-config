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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.addthis.metrics.reporter.config.AbstractRiemannReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.riemann.Riemann;
import com.codahale.metrics.riemann.RiemannReporter;

public class RiemannReporterConfig extends AbstractRiemannReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(RiemannReporterConfig.class);
    private List<RiemannReporter> reporters = new ArrayList<RiemannReporter>();
    private MetricRegistry registry;

    @Override
    public boolean enable(MetricRegistry registry)
    {
        String className = "com.codahale.metrics.riemann.RiemannReporter";
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable RiemannReporter, but class {} was not found", className);
            return false;
        }

        this.registry = registry;

        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable RiemannReporter");
            return false;
        }
        for (HostPort hostPort : hosts)
        {
            try
            {
                log.info("Enabling RiemannReporter to {}:{}", new Object[]{hostPort.getHost(), hostPort.getPort()});
                RiemannReporter.Builder builder =
                RiemannReporter.forRegistry(registry)
                               .convertDurationsTo(getRealDurationunit())
                               .convertRatesTo(getRealRateunit());
                if (prefix != null && !prefix.isEmpty())
                {
                    builder.prefixedWith(prefix);
                }
                if (separator != null && !separator.isEmpty())
                {
                    builder.useSeparator(separator);
                }
                if (localHost != null && !localHost.isEmpty())
                {
                    builder.localHost(localHost);
                }
                if (tags != null && !tags.isEmpty())
                {
                    builder.tags(tags);
                }
                Riemann riemann = new Riemann(hostPort.getHost(), hostPort.getPort());
                RiemannReporter reporter = builder.build(riemann);
                reporter.start(getPeriod(), getRealTimeunit());
                reporters.add(reporter);
            }
            catch (Exception e)
            {
                log.error("Failed to enable RiemannReporter", e);
                return false;
            }
        }
        return true;
    }

    @Override public void report() {
        for (RiemannReporter reporter : reporters)
        {
            reporter.report(registry.getGauges(), registry.getCounters(), registry.getHistograms(), registry.getMeters(), registry.getTimers());
        }
    }

}
