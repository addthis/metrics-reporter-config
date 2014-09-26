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

import java.util.List;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.reporting.GraphiteReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphiteReporterConfig extends AbstractGraphiteReporterConfig implements MetricsReporterConfigTwo
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

    private void enableMetrics2(HostPort hostPort)
    {
        GraphiteReporter.enable(
                Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                hostPort.getHost(), hostPort.getPort(), getResolvedPrefix(),
                MetricPredicateTransformer.generatePredicate(getPredicate()));
    }

    @Override
    public boolean enable()
    {
        boolean success = setup("com.yammer.metrics.reporting.GraphiteReporter");
        if (!success)
        {
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        for (HostPort hostPort : hosts)
        {
            log.info("Enabling GraphiteReporter to {}:{}", new Object[]{hostPort.getHost(), hostPort.getPort()});
            try
            {
                enableMetrics2(hostPort);
            }
            catch (Exception e)
            {
                log.error("Failed to enable GraphiteReporter", e);
                return false;
            }
        }
        return true;
    }
}
