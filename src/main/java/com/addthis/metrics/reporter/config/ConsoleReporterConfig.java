package com.addthis.metrics.reporter.config;

import com.yammer.metrics.reporting.ConsoleReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Use predicate (enable method does not support)
public class ConsoleReporterConfig extends AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(ConsoleReporterConfig.class);

    @Override
    public boolean enable()
    {
        try
        {
            ConsoleReporter.enable(getPeriod(), getRealTimeunit());
        }
        catch (Exception e)
        {
            log.error("Failure while enabling console reporter", e);
            return false;
        }
        return true;
    }

}
