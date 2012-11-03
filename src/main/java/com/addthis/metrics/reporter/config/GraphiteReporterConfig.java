package com.addthis.metrics.reporter.config;

import com.yammer.metrics.Metrics;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphiteReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporterConfig.class);

    @NotNull
    private String prefix = "";

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    @Override
    public boolean enable()
    {
        String className = "com.yammer.metrics.reporting.GraphiteReporter";
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable GraphiteReporter, but class {} was not found", className);
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable GraphiteReporter");
            return false;
        }
        for (HostPort hostPort : hosts)
        {
            try
            {
                log.info("Enabling GraphiteReporter to {}:{}", new Object[] {hostPort.getHost(), hostPort.getPort()});
                com.yammer.metrics.reporting.GraphiteReporter.enable(Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                                                                     hostPort.getHost(), hostPort.getPort(), prefix, getMetricPredicate());

            }
            catch (Exception e)
            {
                log.error("Failed to enable GraphiteReporter", e);
                return false;
            }
        }
        return true;
    }
}
