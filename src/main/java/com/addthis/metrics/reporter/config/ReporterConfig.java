package com.addthis.metrics.reporter.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

// ser/d class.  May make a different abstract for commonalities

// Stupid bean for simplicity and snakeyaml, instead of @Immutable
// like any sane person would intend
public class ReporterConfig {
    private static final Logger log = LoggerFactory.getLogger(ReporterConfig.class);


    private List<ConsoleReporterConfig> console;
    private List<CsvReporterConfig> csv;
    private List<GangliaReporterConfig> ganglia;
    private List<GraphiteReporterConfig> graphite;

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


    public void enableConsole()
    {
        if (console == null) {
            log.warn("Asked to enable console, but it was not configured");
            return;
        }
        for (ConsoleReporterConfig consoleConfig : console) {
            consoleConfig.enable();
        }
    }

    public void enableCsv()
    {
        if (csv == null) {
            log.warn("Asked to enable csv, but it was not configured");
            return;
        }
        for (CsvReporterConfig csvConfig : csv) {
            csvConfig.enable();
        }
    }

    public void enableGanglia()
    {
        if (ganglia == null) {
            log.warn("Asked to enable ganglia, but it was not configured");
            return;
        }
        for (GangliaReporterConfig gangliaConfig : ganglia) {
            gangliaConfig.enable();
        }
    }

    public void enableGraphite()
    {
        if (graphite == null) {
            log.warn("Asked to enable graphite, but it was not configured");
            return;
        }
        for (GraphiteReporterConfig graphiteConfig : graphite) {
            graphiteConfig.enable();
        }
    }


    public void enableAll()
    {
        if (console != null)
            enableConsole();
        if (csv != null)
            enableCsv();
        if (ganglia != null)
            enableGanglia();
        if (graphite != null)
            enableGraphite();
    }

    public static ReporterConfig loadFromFile(String fileName) throws IOException
    {
        Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));
        InputStream input = new FileInputStream(new File(fileName));
        ReporterConfig config = (ReporterConfig) yaml.load(input);
        return config;
    }

}