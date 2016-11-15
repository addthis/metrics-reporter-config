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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.addthis.metrics.reporter.config.AbstractInfluxDBReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricRegistry;
import com.izettle.metrics.influxdb.InfluxDbHttpSender;
import com.izettle.metrics.influxdb.InfluxDbReporter;
import com.izettle.metrics.influxdb.InfluxDbSender;

public class InfluxDBReporterConfig extends AbstractInfluxDBReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(InfluxDBReporterConfig.class);

    private InfluxDbReporter reporter;

    private void enableMetrics3(HostPort hostPort, MetricRegistry registry) throws Exception
    {
        InfluxDbSender influxDbSender = new InfluxDbHttpSender(getProtocol(), hostPort.getHost(), hostPort.getPort(),
            getDbName(), getAuth(), getRealRateunit(), getConnectionTimeout(), getReadTimeout(), getResolvedPrefix());

        reporter = InfluxDbReporter.forRegistry(registry).convertRatesTo(getRealRateunit())
            .convertDurationsTo(getRealDurationunit()).withTags(getResolvedTags())
            .measurementMappings(getMeasurementMappings())
            .filter(MetricFilterTransformer.generateFilter(getPredicate())).build(influxDbSender);

        reporter.start(getPeriod(), getRealTimeunit());
    }

    @Override
    public void report()
    {
        if (reporter != null) {
            reporter.report();
        }
    }

    @Override
    public boolean enable(final MetricRegistry registry)
    {
        boolean success = checkClass("com.izettle.metrics.influxdb.InfluxDbReporter");
        if (!success)
        {
            return false;
        }

        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable InfluxDBReporter");
            return false;
        }

        if(hosts.size() != 1) {
            log.error("Only 1 host can be specified, cannot enable InfluxDBReporter");
            return false;
        }

        HostPort hostPort = hosts.get(0);
        log.info("Enabling InfluxDBReporter to {}:{}", hostPort.getHost(), hostPort.getPort());
        try
        {
          enableMetrics3(hostPort, registry);
        } catch (Exception e)
        {
            log.error("Failed to enable InfluxDBReporter for {}:{}", hostPort.getHost(), hostPort.getPort(), e);
            return false;
        }
        return true;
    }

    private boolean checkClass(String className)
    {
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable InfluxDBReporter, but class {} was not found", className);
            return false;
        } else
            {
            return true;
        }
    }
}
