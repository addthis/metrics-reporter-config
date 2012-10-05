package com.addthis.metrics.reporter.config;

import com.yammer.metrics.reporting.CsvReporter;

import java.io.File;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvReporterConfig extends AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(CsvReporterConfig.class);

    @NotNull
    private String outdir;

    public String getOutdir()
    {
        return outdir;
    }

    public void setOutdir(String outdir)
    {
        this.outdir = outdir;
    }

    @Override
    public void enable()
    {
        log.info("Enabling CsvReporter to {}", outdir);
        File foutDir = new File(outdir);
        foutDir.mkdirs();
        CsvReporter.enable(foutDir, getPeriod(), getRealTimeunit());
    }

}
