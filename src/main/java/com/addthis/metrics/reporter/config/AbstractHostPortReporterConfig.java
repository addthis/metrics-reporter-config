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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractHostPortReporterConfig extends AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractHostPortReporterConfig.class);

    @Valid
    private List<HostPort> hosts;
    @Valid
    private String hostsString;

    public List<HostPort> getHosts()
    {
        return hosts;
    }

    public void setHosts(List<HostPort> hosts)
    {
        this.hosts = hosts;
    }


    public String getHostsString()
    {
        return hostsString;
    }

    public void setHostsString(String hostsString)
    {
        this.hostsString = hostsString;
    }

    public List<HostPort> parseHostString()
    {
        // Done manually to avoid a guava dep
        List<HostPort> hosts = new ArrayList<HostPort>();
        String[] hostPairs = getHostsString().split(",");
        for (int i = 0; i < hostPairs.length; i++)
        {
            String[] pair = hostPairs[i].split(":");
            hosts.add(new HostPort(pair[0], Integer.valueOf(pair[1])));
        }
        return hosts;
    }

    public List<HostPort> getHostListAndStringList()
    {
        // some simple log valadatin' sinc we can't || the @NotNulls
        // make mini protected functions sans logging for Ganglia
        if (getHosts() == null && getHostsString() == null)
        {
            log.warn("No hosts specified as a list or delimited string");
            return null;
        }
        if (getHosts() != null && getHostsString() != null)
        {
            log.warn("Did you really mean to have hosts as a list and delimited string?");
        }
        ArrayList<HostPort> combinedHosts = new ArrayList<HostPort>();
        if (getHosts() != null)
        {
            combinedHosts.addAll(getHosts());
        }
        if (getHostsString() != null)
        {
            combinedHosts.addAll(parseHostString());
        }
        return combinedHosts;

    }

    public abstract List<HostPort> getFullHostList();

}
