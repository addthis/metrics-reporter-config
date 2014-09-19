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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.yammer.metrics.Metrics;

public class GraphiteReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporterConfig.class);

    /**
     * Test constructor
     * 
     * @param localhost
     *            localhost
     */
    GraphiteReporterConfig(InetAddress localhost) {
        super(localhost);
    }

    public GraphiteReporterConfig() {
        super();
    }

    @Override
    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    private void enableMetrics2(HostPort hostPort)
    {
        com.yammer.metrics.reporting.GraphiteReporter.enable(
                Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                hostPort.getHost(), hostPort.getPort(), getResolvedPrefix(), getMetricPredicate());
    }

    private void enableMetrics3(HostPort hostPort, MetricRegistry registry) {
        com.codahale.metrics.graphite.GraphiteReporter.forRegistry(registry)
                .convertRatesTo(getRealRateunit())
                .convertDurationsTo(getRealDurationunit())
                .prefixedWith(getResolvedPrefix())
                .filter(getMetricFilter())
                .build(new com.codahale.metrics.graphite.Graphite(new InetSocketAddress(hostPort.getHost(),
                        hostPort.getPort())))
                .start(getPeriod() ,getRealTimeunit());
    }

    private boolean enable(MetricsVersion version, String className, MetricRegistry registry)
    {
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable GraphiteReporter, but class {} was not found", className);
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable GraphiteReporter");
            return false;
        }
        for (HostPort hostPort : hosts)
        {
            try
            {
                log.info("Enabling GraphiteReporter to {}:{}",
                        new Object[] {hostPort.getHost(), hostPort.getPort()});
                switch (version)
                {
                    case SERIES_2:
                        enableMetrics2(hostPort);
                        break;
                    case SERIES_3:
                        enableMetrics3(hostPort, registry);
                        break;
                }
            }
            catch (Exception e)
            {
                log.error("Failed to enable GraphiteReporter", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean enable2() {
        return enable(MetricsVersion.SERIES_2,
                "com.yammer.metrics.reporting.GraphiteReporter", null);
    }

    @Override
    public boolean enable3(MetricRegistry registry) {
        return enable(MetricsVersion.SERIES_3,
                "com.codahale.metrics.graphite.GraphiteReporter", registry);
    }
}
