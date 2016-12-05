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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractInfluxDBReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractGraphiteReporterConfig.class);

    @NotNull
    private String protocol;

    @NotNull
    private String auth;

    @NotNull
    private String dbName;

    @NotNull
    @Min(0)
    private int connectionTimeout;

    @NotNull
    @Min(0)
    private int readTimeout;

    @NotNull
    private Map<String, String> tags;

    private Map<String, String> resolvedTags;

    @NotNull
    private Map<String, String> measurementMappings = Collections.emptyMap();

    @Override
    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getAuth()
    {
        return auth;
    }

    public void setAuth(String auth)
    {
        this.auth = auth;
    }

    public String getDbName()
    {
        return dbName;
    }

    public void setDbName(String dbName)
    {
        this.dbName = dbName;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
        this.resolvedTags = new HashMap<String, String>(tags.size());
        for (Map.Entry<String, String> entry : tags.entrySet())
        {
            this.resolvedTags.put(entry.getKey(), resolvePrefix(entry.getValue()));
        }
    }

    public Map<String, String> getResolvedTags()
    {
        return resolvedTags;
    }

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    public Map<String, String> getMeasurementMappings() { return measurementMappings; }

    public void setMeasurementMappings(Map<String, String> measurementMappings) { this.measurementMappings = measurementMappings; }
}
