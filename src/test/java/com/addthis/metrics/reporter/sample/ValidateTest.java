package com.addthis.metrics.reporter.sample;

import com.addthis.metrics.reporter.config.HostPort;
import com.addthis.metrics.reporter.config.ReporterConfig;

import java.io.IOException;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidateTest
{
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void validateSamples() throws IOException
    {
        ReporterConfig.loadFromFileAndValidate("src/test/resources/sample/console.yaml");
        ReporterConfig.loadFromFileAndValidate("src/test/resources/sample/csv.yaml");
        ReporterConfig.loadFromFileAndValidate("src/test/resources/sample/ganglia.yaml");
        ReporterConfig.loadFromFileAndValidate("src/test/resources/sample/graphite.yaml");
        ReporterConfig.loadFromFileAndValidate("src/test/resources/sample/multi.yaml");
    }

    @Test
    public void validationWorks() throws IOException
    {
        assertFalse(ReporterConfig.validate(new HostPort()));
    }


    @Test
    public void validateMissingPeriod() throws IOException
    {
        thrown.expect(ReporterConfig.ReporterConfigurationException.class);
        ReporterConfig config = ReporterConfig.loadFromFileAndValidate("src/test/resources/invalid/missing-period.yaml");
    }

    @Test
    public void validateMissingOutDir() throws IOException
    {
        thrown.expect(ReporterConfig.ReporterConfigurationException.class);
        ReporterConfig config = ReporterConfig.loadFromFileAndValidate("src/test/resources/invalid/csv-missing-outdir.yaml");
    }


    @Test
    public void validateMissingPortRange() throws IOException
    {
        thrown.expect(ReporterConfig.ReporterConfigurationException.class);
        ReporterConfig config = ReporterConfig.loadFromFileAndValidate("src/test/resources/invalid/invalid-port-range.yaml");
    }


}
