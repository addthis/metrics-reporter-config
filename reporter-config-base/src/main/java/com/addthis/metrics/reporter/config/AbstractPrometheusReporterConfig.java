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
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractPrometheusReporterConfig extends AbstractMetricReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractPrometheusReporterConfig.class);

    public boolean ssl;
    public String host;
    public int port;
    public List<Mapping> mappings;
    public List<Exclusion> exclusions;

    public boolean isSsl()
    {
        return ssl;
    }

    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public List<Mapping> getMappings()
    {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings)
    {
        this.mappings = mappings;
    }

    public List<Exclusion> getExclusions()
    {
        return exclusions;
    }

    public void setExclusions(List<Exclusion> exclusions)
    {
        this.exclusions = exclusions;
    }

    public final static class Mapping {
        public Pattern regex;
        public String pattern;
        public String name;
        public List<Label> labels = new ArrayList<Label>();

        public String getPattern()
        {
            return pattern;
        }

        public void setPattern(String pattern)
        {
            this.pattern = pattern;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public List<Label> getLabels()
        {
            return labels;
        }

        public void setLabels(List<Label> labels)
        {
            this.labels = labels;
        }
    }

    public final static class Exclusion {
        public Pattern regex;
        public String pattern;

        public String getPattern()
        {
            return pattern;
        }

        public void setPattern(String pattern)
        {
            this.pattern = pattern;
        }
    }

    public final static class Label {
        public String label;
        public String value;

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
}
