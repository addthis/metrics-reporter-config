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
    private List<? extends ConfiguredReporter> reporters;

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


    public List<? extends ConfiguredReporter> getReporters()
    {
    	return reporters;
    }
    
    public void setReporters(List<? extends ConfiguredReporter> reporters)
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
    	for (ConfiguredReporter reporter : reporters)
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
}
