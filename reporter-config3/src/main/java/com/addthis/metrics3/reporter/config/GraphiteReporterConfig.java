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

import java.net.InetSocketAddress;

import java.util.List;

import com.addthis.metrics.reporter.config.AbstractGraphiteReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.codahale.metrics.graphite.PickledGraphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphiteReporterConfig extends AbstractGraphiteReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporterConfig.class);

    private GraphiteReporter reporter;

    private void enableMetrics3(HostPort hostPort, MetricRegistry registry)
    {
        InetSocketAddress addr = new InetSocketAddress(hostPort.getHost(), hostPort.getPort());
        GraphiteSender sender;
        if (isPickled())
        {
            sender = isUdp() ? newPickledGraphiteUDP(addr) : new PickledGraphite(addr);
        }
        else
        {
            sender = isUdp() ? new GraphiteUDP(addr) : new Graphite(addr);
        }

        reporter = GraphiteReporter.forRegistry(registry)
                .convertRatesTo(getRealRateunit())
                .convertDurationsTo(getRealDurationunit())
                .prefixedWith(getResolvedPrefix())
                .filter(MetricFilterTransformer.generateFilter(getPredicate()))
                .build(sender);
        reporter.start(getPeriod(), getRealTimeunit());
    }

    // using reflection until PickledGraphiteUDP is merged, released, and depended upon
    private static GraphiteSender newPickledGraphiteUDP(InetSocketAddress addr)
    {
        try
        {
            return (GraphiteSender) Class.forName("com.codahale.metrics.graphite.PickledGraphiteUDP")
                    .getConstructor(InetSocketAddress.class)
                    .newInstance(addr);
        }
        catch (ReflectiveOperationException ex)
        {
            throw new IllegalStateException("PickledGraphiteUDP not found in metrics-graphite jar file");
        }
    }

    @Override
    public void report() {
        if (reporter != null) {
            reporter.report();
        }
    }

    @Override
    public boolean enable(MetricRegistry registry)
    {
        boolean success = setup("com.codahale.metrics.graphite.GraphiteReporter");
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
                enableMetrics3(hostPort, registry);
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
