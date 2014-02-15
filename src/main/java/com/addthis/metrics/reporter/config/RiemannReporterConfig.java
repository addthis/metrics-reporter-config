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

import com.yammer.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RiemannReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(RiemannReporterConfig.class);

    private String localHost;
    private String prefix;
    private String separator;
    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public List<HostPort> getFullHostList()
    {
             return getHostListAndStringList();
    }


    @Override
    public boolean enable()
    {
        String className = "com.yammer.metrics.reporting.RiemannReporter";
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable RiemannReporter, but class {} was not found", className);
            return false;
        }
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
                com.yammer.metrics.reporting.RiemannReporter.ConfigBuilder builder =
                    com.yammer.metrics.reporting.RiemannReporter.Config.newBuilder()
                    .metricsRegistry(Metrics.defaultRegistry())
                    .period(getPeriod())
                    .unit(getRealTimeunit())
                    .host(hostPort.getHost())
                    .port(hostPort.getPort())
                    .predicate(getMetricPredicate());
                if (prefix != null && !prefix.isEmpty())
                {
                    builder.prefix(prefix);
                }
                if (separator != null && !separator.isEmpty())
                {
                    builder.separator(separator);
                }
                if (localHost != null && !localHost.isEmpty())
                {
                    builder.localHost(localHost);
                }
                if (tags != null && !tags.isEmpty())
                {
                    builder.tags(tags);
                }
                com.yammer.metrics.reporting.RiemannReporter.enable(builder.build());
            }
            catch (Exception e)
            {
                log.error("Failed to enable RiemannReporter", e);
                return false;
            }
        }
        return true;
    }
}
