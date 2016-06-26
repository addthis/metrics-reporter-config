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

import com.addthis.metrics.reporter.config.AbstractZabbixReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

public class ZabbixReporterConfig extends AbstractZabbixReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(ZabbixReporterConfig.class);
    private static final String SENDER_CLASS = "io.github.hengyunabc.zabbix.sender.ZabbixSender";

    private final List<ZabbixReporter> reporters = new ArrayList<ZabbixReporter>();
    private MetricRegistry registry;

    @Override
    public boolean enable(MetricRegistry registry)
    {
        if (!isClassAvailable(SENDER_CLASS))
        {
            log.error("Tried to enable Zabbix Reporter, but class {} was not found", SENDER_CLASS);
            return false;
        }

        this.registry = registry;

        if (hostName == null)
            hostName = System.getenv("COMPUTERNAME");
        if (hostName == null)
            hostName = System.getenv("HOSTNAME");
        if (hostName == null)
            hostName = "localhost";
        // note: don't go via InetAddress.getLocalHost() as that may return unexpected results or
        // take a long time (DNS roundtrips/timeouts) or even an exception being throws,

        for (HostPort hostPort : getFullHostList())
        {
            ZabbixSender sender = new ZabbixSender(hostPort.getHost(), hostPort.getPort(), connectTimeout, socketTimeout);
            MetricFilter filter = MetricFilterTransformer.generateFilter(getPredicate());
            ZabbixReporter reporter = new ZabbixReporter(sender, hostName, prefix, registry, name, filter, getRealRateunit(), getRealDurationunit());
            reporters.add(reporter);
        }

        return true;
    }

    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    public void report()
    {
        // noop
    }
}
