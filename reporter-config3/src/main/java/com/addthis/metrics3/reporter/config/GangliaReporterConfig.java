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

import java.io.IOException;

import java.util.List;

import com.addthis.metrics.reporter.config.AbstractGangliaReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ganglia.GangliaReporter;

import info.ganglia.gmetric4j.gmetric.GMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GangliaReporterConfig extends AbstractGangliaReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(GangliaReporterConfig.class);

    private void enableMetrics3(HostPort hostPort, MetricRegistry registry) throws IOException {
        /**
         * browsing through https://github.com/ganglia/gmetric4j it appears that the
         * "ttl" parameter is ignored for GMetric.UDPAddressingMode.UNICAST
         */
        GangliaReporter.forRegistry(registry)
        .convertRatesTo(getRealRateunit())
        .convertDurationsTo(getRealDurationunit())
        .prefixedWith(groupPrefix)
        .filter(MetricFilterTransformer.generateFilter(getPredicate()))
        .build(new GMetric(hostPort.getHost(), hostPort.getPort(),
                GMetric.UDPAddressingMode.UNICAST, 1))
        .start(getPeriod(), getRealTimeunit());
    }

    @Override
    public boolean enable(MetricRegistry registry)
    {
        boolean success = setup("com.codahale.metrics.ganglia.GangliaReporter");
        if (!success)
        {
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        for (HostPort hostPort : hosts)
        {
            log.info("Enabling GangliaReporter to {}:{}", new Object[]{hostPort.getHost(), hostPort.getPort()});
            try
            {
                enableMetrics3(hostPort, registry);
            }
            catch (Exception e)
            {
                log.error("Faliure while enabling GangliaReporter", e);
                return false;
            }
        }
        return true;
    }

}
