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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractHostPortReporterConfig extends AbstractMetricReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractHostPortReporterConfig.class);

    public static final String MACRO_HOST_NAME = "host.name";
    public static final String MACRO_HOST_ADDRESS = "host.address";
    public static final String MACRO_HOST_FQDN = "host.fqdn";
    public static final String MACRO_HOST_NAME_SHORT = "host.name.short";

    @Valid
    private List<HostPort> hosts;
    @Valid
    private String hostsString;
    @NotNull
    private String prefix = "";

    private InetAddress localhost;
    private String resolvedPrefix;

    /**
     * Test constructor
     *
     * @param localhost
     *            localhost
     */
    AbstractHostPortReporterConfig(InetAddress localhost) {
        this.localhost = localhost;
    }

    public AbstractHostPortReporterConfig() {
        try {
            this.localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // not expected to happen with properly configured system
            log.error("Unable to get localhost", e);
        }
    }

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

    public String getResolvedPrefix() {
        return resolvedPrefix;
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

    /**
     * <p>
     * Sets the prefix to be prepended to all metric names. The prefix may
     * contain the variable references in the following format: ${macro_name}.
     * <p>
     * The following macros are supported:
     * <dl>
     * <dt>{@link #MACRO_HOST_ADDRESS}</dt>
     * <dd>The value returned for local host by
     * {@link java.net.InetAddress#getHostAddress()}</dd>
     * <dt>{@link #MACRO_HOST_NAME}</dt>
     * <dd>The value returned for local host by
     * {@link java.net.InetAddress#getHostName()}</dd>
     * <dt>{@link #MACRO_HOST_NAME_SHORT}</dt>
     * <dd>The value returned for local host by
     * {@link java.net.InetAddress#getHostName()} up to first dot</dd>
     * <dt>{@link #MACRO_HOST_FQDN}</dt>
     * <dd>The value returned for local host by
     * {@link java.net.InetAddress#getCanonicalHostName()}</dd>
     * </dl>
     *
     * <p>
     * All substituted values are made metric-safe
     *
     * @param prefix
     *            prefix value
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.resolvedPrefix = resolvePrefix(prefix);
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    String resolvePrefix(String prefixTemplate) {
        Map<String, String> valueMap = new HashMap<String, String>();
        if (localhost != null) {
            String hostname = localhost.getHostName();
            valueMap.put(MACRO_HOST_NAME, sanitizeName(hostname));
            if (!StringUtils.isEmpty(hostname)) {
                valueMap.put(MACRO_HOST_NAME_SHORT,
                        sanitizeName(StringUtils.split(hostname, '.')[0]));
            }
            valueMap.put(MACRO_HOST_ADDRESS,
                    sanitizeName(localhost.getHostAddress()));
            valueMap.put(MACRO_HOST_FQDN,
                    sanitizeName(localhost.getCanonicalHostName()));
        }

        return StrSubstitutor.replace(prefixTemplate, valueMap);
    }


}
