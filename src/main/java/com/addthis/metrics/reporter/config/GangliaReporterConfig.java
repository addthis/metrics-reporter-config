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


import java.util.List;

import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import info.ganglia.gmetric4j.gmetric.GMetric;
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


    public String getGroupPrefix()
    {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix)
    {
        this.groupPrefix = groupPrefix;
    }

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


    @Override
    public boolean enable(MetricRegistry registry)
    {
        String className = "com.codahale.metrics.ganglia.GangliaReporter";
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
                com.codahale.metrics.ganglia.GangliaReporter.forRegistry(registry)
                        .prefixedWith(groupPrefix)
                        .filter(getMetricPredicate())
                        .build(new info.ganglia.gmetric4j.gmetric.GMetric(hostPort.getHost(), hostPort.getPort(),
                                info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode.MULTICAST, 1))
                        .start(getPeriod(), getRealTimeunit());
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
