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

import com.codahale.metrics.MetricRegistry;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GangliaReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(GangliaReporterConfig.class);

    @NotNull
    private String groupPrefix = "";
    @NotNull
    private boolean compressPackageNames = false;
    private String gmondConf;

    public boolean getCompressPackageNames()
    {
        return compressPackageNames;
    }

    public void setCompressPackageNames(boolean compressPackageNames)
    {
        this.compressPackageNames = compressPackageNames;
    }

    public String getGmondConf()
    {
        return gmondConf;
    }

    public void setGmondConf(String gmondConf)
    {
        this.gmondConf = gmondConf;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    @Override
    public List<HostPort> getFullHostList()
    {
         if (gmondConf != null)
         {
             GmondConfigParser gcp = new GmondConfigParser();
             List<HostPort> confHosts = gcp.getGmondSendChannels(gmondConf);
             if (confHosts == null || confHosts.isEmpty())
             {
                 log.warn("No send channels found after reading {}", gmondConf);
             }
             return confHosts;
         }
         else
         {
             return getHostListAndStringList();
         }
    }

    private void enableMetrics2(HostPort hostPort) throws InvocationTargetException,
                                                          IllegalAccessException,
                                                          NoSuchMethodException
    {
        try
        {
            /**
             * This will only be invoked if using a fork of the 2.2.0 metrics library with support
             * for ganglia metric prefixes (in addition to the regular group prefixes):
             * http://github.com/mspiegel/metrics. Otherwise the regular ganglia reporter is enabled.
             */
            Method enable = com.yammer.metrics.reporting.GangliaReporter.class.getDeclaredMethod(
                    "enable", MetricsRegistry.class,
                    Long.TYPE, TimeUnit.class, String.class, Integer.TYPE, String.class, String.class,
                    MetricPredicate.class, Boolean.TYPE);
            enable.invoke(null, Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                    hostPort.getHost(), hostPort.getPort(), resolvePrefix(groupPrefix),
                    getResolvedPrefix(), getMetricPredicate(), compressPackageNames);
        }
        catch(NoSuchMethodException ex)
        {
            Method enable = com.yammer.metrics.reporting.GangliaReporter.class.getDeclaredMethod(
                    "enable", MetricsRegistry.class,
                    Long.TYPE, TimeUnit.class, String.class, Integer.TYPE, String.class,
                    MetricPredicate.class, Boolean.TYPE);
            enable.invoke(null, Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                    hostPort.getHost(), hostPort.getPort(), resolvePrefix(groupPrefix),
                    getMetricPredicate(), compressPackageNames);
        }
    }

    private void enableMetrics3(HostPort hostPort, MetricRegistry registry) throws IOException {
        com.codahale.metrics.ganglia.GangliaReporter.forRegistry(registry)
        .convertRatesTo(getRealRateunit())
        .convertDurationsTo(getRealDurationunit())
        .prefixedWith(groupPrefix)
        .filter(getMetricFilter())
        .build(new info.ganglia.gmetric4j.gmetric.GMetric(hostPort.getHost(), hostPort.getPort(),
                info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode.MULTICAST, 1))
        .start(getPeriod(), getRealTimeunit());
    }

    private boolean enable(MetricsVersion version, String className, MetricRegistry registry)
    {
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable GangliaReporter, but class {} was not found", className);
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable GangliaReporter");
            return false;
        }
        for (HostPort hostPort : hosts)
        {
            log.info("Enabling GangliaReporter to {}:{}", new Object[] {hostPort.getHost(), hostPort.getPort()});
            try
            {
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
                log.error("Faliure while enabling GangliaReporter", e);
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean enable()
    {
        return enable(MetricsVersion.SERIES_2,
                "com.yammer.metrics.reporting.GangliaReporter", null);
    }


    @Override
    public boolean enable(MetricRegistry registry)
    {
        return enable(MetricsVersion.SERIES_3,
                "com.codahale.metrics.ganglia.GangliaReporter", registry);
    }
}
