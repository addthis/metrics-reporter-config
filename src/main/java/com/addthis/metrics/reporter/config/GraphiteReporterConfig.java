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


import java.net.InetSocketAddress;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphiteReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporterConfig.class);

    @NotNull
    private String prefix = "";

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    @Override
    public boolean enable(MetricRegistry registry)
    {
        String className = "com.codahale.metrics.graphite.GraphiteReporter";
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
                log.info("Enabling GraphiteReporter to {}:{}", new Object[] {hostPort.getHost(), hostPort.getPort()});
                com.codahale.metrics.graphite.GraphiteReporter.forRegistry(registry)
                    .prefixedWith(prefix)
                    .filter(getMetricPredicate())
                    .build(new com.codahale.metrics.graphite.Graphite(new InetSocketAddress(hostPort.getHost(),
                    hostPort.getPort())))
                .start(getPeriod() ,getRealTimeunit());

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
