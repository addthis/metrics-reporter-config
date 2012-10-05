package com.addthis.metrics.reporter.config;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GangliaReporterConfig extends AbstractReporterConfig {
    private static final Logger log = LoggerFactory.getLogger(GangliaReporterConfig.class);

    @Valid
    @NotNull
    private List<HostPort> hosts;
    @NotNull
    private String groupPrefix = "";
    @NotNull
    private boolean compressPackageNames = false;

    public List<HostPort> getHosts()
    {
        return hosts;
    }

    public void setHosts(List<HostPort> hosts) {
        this.hosts = hosts;
    }

    public String getGroupPrefix()
    {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix)
    {
        this.groupPrefix = groupPrefix;
    }

    public boolean getCompressPackageNames()
    {
        return compressPackageNames;
    }

    public void setCompressPackageNames(boolean compressPackageNames)
    {
        this.compressPackageNames = compressPackageNames;
    }


    @Override
    public void enable()
    {
        String className = "com.yammer.metrics.reporting.GangliaReporter";
        if (!isClassAvailable(className)) {
            log.error("Tried to enable GangliaReporter, but class {} was not found", className);
            return;
        }
        if (hosts == null || hosts.isEmpty()) {
            log.error("No hosts specified, cannot enable GangliaReporter");
            return;
        }
        for (HostPort hostPort : hosts) {
            log.info("Enabling GangliaReporter to {}:{}", new Object[] {hostPort.getHost(), hostPort.getPort()});
            com.yammer.metrics.reporting.GangliaReporter.enable(Metrics.defaultRegistry(), getPeriod(), getRealTimeunit(),
                                                                hostPort.getHost(), hostPort.getPort(), groupPrefix,
                                                                MetricPredicate.ALL, compressPackageNames);

        }
    }
}
