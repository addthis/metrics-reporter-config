package com.addthis.metrics.reporter.config;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphiteReporterConfig extends AbstractReporterConfig {
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporterConfig.class);

    private List<HostPort> hosts;
    private String prefix = "";

    public List<HostPort> getHosts()
    {
        return hosts;
    }

    public void setHosts(List<HostPort> hosts) {
        this.hosts = hosts;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }


    @Override
    public void enable()
    {
        String className = "com.yammer.metrics.reporting.GraphiteReporter";
        if (!isClassAvailable(className)) {
            log.error("Tried to enable GraphiteReporter, but class {} was not found", className);
            return;
        }
        if (hosts == null || hosts.isEmpty()) {
            log.error("No hosts specified, cannot enable GraphiteReporter");
            return;
        }
        for (HostPort hostPort : hosts) {
            log.info("Enabling GraphiteReporter to {}:{}", new Object[] {hostPort.getHost(), hostPort.getPort()});
            com.yammer.metrics.reporting.GraphiteReporter.enable(getPeriod(), getRealTimeunit(),
                                                                 hostPort.getHost(), hostPort.getPort(), prefix);
        }
    }
}
