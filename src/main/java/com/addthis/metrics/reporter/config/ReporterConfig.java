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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


// ser/d class.  May make a different abstract for commonalities

// Stupid bean for simplicity and snakeyaml, instead of @Immutable
// like any sane person would intend
public class ReporterConfig
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

    public boolean enableConsole2()
    {
        return enableConsole(MetricsVersion.SERIES_2, null);
    }

    public boolean enableConsole3(MetricRegistry registry)
    {
        return enableConsole(MetricsVersion.SERIES_3, registry);
    }

    private boolean enableConsole(MetricsVersion version, MetricRegistry registry)
    {
        boolean failures = false;
        if (console == null)
        {
            log.debug("Asked to enable console, but it was not configured");
            return false;
        }
        for (ConsoleReporterConfig consoleConfig : console)
        {
            boolean success = (version == MetricsVersion.SERIES_2) ?
                              consoleConfig.enable2() :
                              consoleConfig.enable3(registry);
            if (!success)
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableCsv2()
    {
        return enableCsv(MetricsVersion.SERIES_2, null);
    }

    public boolean enableCsv3(MetricRegistry registry)
    {
        return enableCsv(MetricsVersion.SERIES_3, registry);
    }

    private boolean enableCsv(MetricsVersion version, MetricRegistry registry)
    {
        boolean failures = false;
        if (csv == null)
        {
            log.debug("Asked to enable csv, but it was not configured");
            return false;
        }
        for (CsvReporterConfig csvConfig : csv)
        {
            boolean success = (version == MetricsVersion.SERIES_2) ?
                              csvConfig.enable2() :
                              csvConfig.enable3(registry);
            if (!success)
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableGanglia2()
    {
        return enableGanglia(MetricsVersion.SERIES_2, null);
    }

    public boolean enableGanglia3(MetricRegistry registry)
    {
        return enableGanglia(MetricsVersion.SERIES_3, registry);
    }

    private boolean enableGanglia(MetricsVersion version, MetricRegistry registry)
    {
        boolean failures = false;
        if (ganglia == null)
        {
            log.debug("Asked to enable ganglia, but it was not configured");
            return false;
        }
        for (GangliaReporterConfig gangliaConfig : ganglia)
        {
            boolean success = (version == MetricsVersion.SERIES_2) ?
                              gangliaConfig.enable2() :
                              gangliaConfig.enable3(registry);
            if (!success)
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableGraphite2()
    {
        return enableGraphite(MetricsVersion.SERIES_2, null);
    }

    public boolean enableGraphite3(MetricRegistry registry)
    {
        return enableGraphite(MetricsVersion.SERIES_3, registry);
    }

    private boolean enableGraphite(MetricsVersion version, MetricRegistry registry)
    {
        boolean failures = false;
        if (graphite == null)
        {
            log.debug("Asked to enable graphite, but it was not configured");
            return false;
        }
        for (GraphiteReporterConfig graphiteConfig : graphite)
        {
            boolean success = (version == MetricsVersion.SERIES_2) ?
                              graphiteConfig.enable2() :
                              graphiteConfig.enable3(registry);
            if (!success)
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableRiemann2()
    {
        return enableRiemann(MetricsVersion.SERIES_2, null);
    }

    private boolean enableRiemann3(MetricRegistry registry)
    {
        return enableRiemann(MetricsVersion.SERIES_3, registry);
    }

    private boolean enableRiemann(MetricsVersion version, MetricRegistry registry)
    {
        boolean failures = false;
        if (riemann == null)
        {
            log.debug("Asked to enable riemann, but it was not configured");
            return false;
        }
        for (RiemannReporterConfig riemannConfig : riemann)
        {
            boolean success = (version == MetricsVersion.SERIES_2) ?
                              riemannConfig.enable2() :
                              riemannConfig.enable3(registry);
            if (!success)
            {
                failures = true;
            }
        }
        return !failures;
    }

    public boolean enableAll2()
    {
        return enableAll(MetricsVersion.SERIES_2, null);
    }

    public boolean enableAll3(MetricRegistry registry)
    {
        return enableAll(MetricsVersion.SERIES_3, registry);
    }

    private boolean enableAll(MetricsVersion version, MetricRegistry registry)
    {
        boolean enabled = false;
        if (console != null)
        {
            if (enableConsole(version, registry))
            {
                enabled = true;
            }
        }
        if (csv != null)
        {
            if (enableCsv(version, registry))
            {
                enabled = true;
            }
        }
        if (ganglia != null)
        {
            if (enableGanglia(version, registry))
            {
                enabled = true;
            }
        }
        if (graphite != null)
        {
            if (enableGraphite(version, registry))
            {
                enabled = true;
            }
        }
        if (riemann != null)
        {
            if (enableRiemann(version, registry))
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
        Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));
        InputStream input = new FileInputStream(new File(fileName));
        ReporterConfig config = (ReporterConfig) yaml.load(input);
        return config;
    }


    // Based on com.yammer.dropwizard.validation.Validator
    public static <T> boolean validate(T obj)
    {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Set<ConstraintViolation<T>> violations = factory.getValidator().validate(obj);
        final SortedSet<String> errors = new TreeSet<String>();
        for (ConstraintViolation<T> v : violations)
        {
            errors.add(String.format("%s %s (was %s)",
                                     v.getPropertyPath(),
                                     v.getMessage(),
                                     v.getInvalidValue()));
        }
        if (errors.isEmpty())
        {
            return true;
        }
        else
        {
            log.error("Failed to validate: {}", errors);
            return false;
        }
    }

    public static class ReporterConfigurationException extends RuntimeException
    {
        public ReporterConfigurationException(String msg)
        {
            super(msg);
        }
    }


    /**
     * This enable Metrics 2.x reporting.
     *
     * @deprecated Use {@link #enableConsole2()} for Metrics 2.x
     * or {@link #enableConsole3(com.codahale.metrics.MetricRegistry)}
     * for Metrics 3.x.
     */
    public boolean enableConsole()
    {
        return enableConsole2();
    }

    /**
     * This enable Metrics 2.x reporting.
     *
     * @deprecated Use {@link #enableCsv2()} for Metrics 2.x
     * or {@link #enableCsv3(com.codahale.metrics.MetricRegistry)}
     * for Metrics 3.x.
     */
    public boolean enableCsv()
    {
        return enableCsv2();
    }

    /**
     * This enable Metrics 2.x reporting.
     *
     * @deprecated Use {@link #enableGanglia2()} for Metrics 2.x
     * or {@link #enableGanglia3(com.codahale.metrics.MetricRegistry)}
     * for Metrics 3.x.
     */
    public boolean enableGanglia()
    {
        return enableGanglia2();
    }

    /**
     * This enable Metrics 2.x reporting.
     *
     * @deprecated Use {@link #enableGraphite2()} for Metrics 2.x
     * or {@link #enableGraphite3(com.codahale.metrics.MetricRegistry)}
     * for Metrics 3.x.
     */
    public boolean enableGraphite()
    {
        return enableGraphite2();
    }

    /**
     * This enable Metrics 2.x reporting.
     *
     * @deprecated Use {@link #enableRiemann2()} for Metrics 2.x
     * or {@link #enableRiemann3(com.codahale.metrics.MetricRegistry)}
     * for Metrics 3.x.
     */
    public boolean enableRiemann()
    {
        return enableRiemann2();
    }


    /**
     * This enable Metrics 2.x reporting.
     *
     * @deprecated Use {@link #enableAll2()} for Metrics 2.x
     * or {@link #enableAll3(com.codahale.metrics.MetricRegistry)}
     * for Metrics 3.x.
     */
    public boolean enableAll()
    {
        return enableAll2();
    }

}
