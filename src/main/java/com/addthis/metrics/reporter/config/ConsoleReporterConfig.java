package com.addthis.metrics.reporter.config;

import com.yammer.metrics.reporting.ConsoleReporter;

public class ConsoleReporterConfig extends AbstractReporterConfig
{

    @Override
    public void enable()
    {
        ConsoleReporter.enable(getPeriod(), getRealTimeunit());
    }

}
