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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;

public class GraphiteReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporterConfig.class);

    public static final String MACRO_HOST_NAME = "host.name";
    public static final String MACRO_HOST_ADDRESS = "host.address";
    public static final String MACRO_HOST_FQDN = "host.fqdn";
    public static final String MACRO_HOST_NAME_SHORT = "host.name.short";

    private InetAddress localhost;
    private String resolvedPrefix;

    /**
     * Test constructor
     * 
     * @param localhost
     *            localhost
     */
    GraphiteReporterConfig(InetAddress localhost) {
        this.localhost = localhost;
    }

    public GraphiteReporterConfig() {
        try {
            this.localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // not expected to happen with properly configured system
            log.error("Unable to get localhost", e);
        }
    }
    
    @NotNull
    private String prefix = "";

    public String getPrefix()
    {
        return prefix;
    }

    /**
     * <p>
     * Sets the prefix to be prepended to all metric names. The prefix may
     * contain the variable references in the following format: ${macro_name}.
     * <p>
     * The following macros are supported:
     * <dl>
     * <dt>{@link #MACRO_HOST_ADDRESS}</dt>
     * <dd>The value returned for local host by
     * {@link InetAddress#getHostAddress()}</dd>
     * <dt>{@link #MACRO_HOST_NAME}</dt>
     * <dd>The value returned for local host by
     * {@link InetAddress#getHostName()}</dd>
     * <dt>{@link #MACRO_HOST_NAME_SHORT}</dt>
     * <dd>The value returned for local host by
     * {@link InetAddress#getHostName()} up to first dot</dd>
     * <dt>{@link #MACRO_HOST_FQDN}</dt>
     * <dd>The value returned for local host by
     * {@link InetAddress#getCanonicalHostName()}</dd>
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

    private String resolvePrefix(String prefixTemplate) {
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
	
    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    String getResolvedPrefix() {
        return this.resolvedPrefix;
    }

    @Override
    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    @Override
    public boolean enable()
    {
        String className = "com.yammer.metrics.reporting.GraphiteReporter";
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
                com.yammer.metrics.reporting.GraphiteReporter.enable(Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                                                                     hostPort.getHost(), hostPort.getPort(), resolvedPrefix, getMetricPredicate());

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
