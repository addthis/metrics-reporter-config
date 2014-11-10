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

import javax.validation.Valid;

import java.io.IOException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// ser/d class.  May make a different abstract for commonalities

// Stupid bean for simplicity and snakeyaml, instead of @Immutable
// like any sane person would intend
public class ReporterConfig extends AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(ReporterConfig.class);

    @Valid
    private List<ConsoleReporterConfig> console;
    @Valid
    private List<CsvReporterConfig> csv;
    @Valid
    private List<GangliaReporterConfig> ganglia;
    @Valid
    private List<GraphiteReporterConfig> graphite;
    @Valid
    private List<RiemannReporterConfig> riemann;
    @Valid
    private List<? extends MetricsReporterConfigTwo> reporters;

    public List<ConsoleReporterConfig> getConsole()
    {
        return console;
    }

    public void setConsole(List<ConsoleReporterConfig> console)
    {
        this.console = console;
    }

    public List<CsvReporterConfig> getCsv()
    {
        return csv;
    }

    public void setCsv(List<CsvReporterConfig> csv)
    {
        this.csv = csv;
    }

    public List<GangliaReporterConfig> getGanglia()
    {
        return ganglia;
    }

    public void setGanglia(List<GangliaReporterConfig> ganglia)
    {
        this.ganglia = ganglia;
    }

    public List<GraphiteReporterConfig> getGraphite()
    {
        return graphite;
    }

    public void setGraphite(List<GraphiteReporterConfig> graphite)
    {
        this.graphite = graphite;
    }

    public List<RiemannReporterConfig> getRiemann()
    {
        return riemann;
    }

    public void setRiemann(List<RiemannReporterConfig> riemann)
    {
        this.riemann = riemann;
    }

    public List<? extends MetricsReporterConfigTwo> getReporters()
    {
        return reporters;
    }

    public void setReporters(List<? extends MetricsReporterConfigTwo> reporters)
    {
        this.reporters = reporters;
    }

    public boolean enableConsole()
    {
        boolean failures = false;
        if (console == null)
        {
            log.debug("Asked to enable console, but it was not configured");
            return false;
        }
        for (ConsoleReporterConfig consoleConfig : console)
        {
            if (!consoleConfig.enable())
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableCsv()
    {
        boolean failures = false;
        if (csv == null)
        {
            log.debug("Asked to enable csv, but it was not configured");
            return false;
        }
        for (CsvReporterConfig csvConfig : csv)
        {
            if (!csvConfig.enable())
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableGanglia()
    {
        boolean failures = false;
        if (ganglia == null)
        {
            log.debug("Asked to enable ganglia, but it was not configured");
            return false;
        }
        for (GangliaReporterConfig gangliaConfig : ganglia)
        {
            if (!gangliaConfig.enable())
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableGraphite()
    {
        boolean failures = false;
        if (graphite == null)
        {
            log.debug("Asked to enable graphite, but it was not configured");
            return false;
        }
        for (GraphiteReporterConfig graphiteConfig : graphite)
        {
            if (!graphiteConfig.enable())
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableRiemann()
    {
        boolean failures = false;
        if (riemann == null)
        {
            log.debug("Asked to enable riemann, but it was not configured");
            return false;
        }
        for (RiemannReporterConfig riemannConfig : riemann)
        {
            if (!riemannConfig.enable())
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableReporters()
    {
        boolean failures = false;
        if (reporters == null)
        {
            log.debug("Asked to enable other, but it was not configured");
            return false;
        }
        for (MetricsReporterConfigTwo reporter : reporters)
        {
            if (!reporter.enable())
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableAll()
    {
        boolean enabled = false;
        if (console != null)
        {
            if (enableConsole())
            {
                enabled = true;
            }
        }
        if (csv != null)
        {
            if (enableCsv())
            {
                enabled = true;
            }
        }
        if (ganglia != null)
        {
            if (enableGanglia())
            {
                enabled = true;
            }
        }
        if (graphite != null)
        {
            if (enableGraphite())
            {
                enabled = true;
            }
        }
        if (riemann != null)
        {
            if (enableRiemann())
            {
                enabled = true;
            }
        }
        if (reporters != null)
        {
            if (enableReporters())
            {
                enabled = true;
            }
        }
        if (!enabled)
        {
            log.warn("No reporters were succesfully enabled");
        }
        return enabled;
    }

    public static ReporterConfig loadFromFileAndValidate(String fileName) throws IOException
    {
        ReporterConfig config = loadFromFile(fileName);
        if (validate(config))
        {
            return config;
        }
        else
        {
            throw new ReporterConfigurationException("configuration failed validation");
        }
    }

    public static ReporterConfig loadFromFile(String fileName) throws IOException
    {
        return AbstractReporterConfig.loadFromFile(fileName, ReporterConfig.class);
    }

}
