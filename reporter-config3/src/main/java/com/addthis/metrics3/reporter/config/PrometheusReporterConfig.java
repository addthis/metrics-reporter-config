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

package com.addthis.metrics3.reporter.config;

import java.util.ArrayList;
import java.util.List;

import com.addthis.metrics.reporter.config.AbstractPrometheusReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.addthis.metrics3.reporter.config.prometheus.MetricRegistryDecorator;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;

public class PrometheusReporterConfig extends AbstractPrometheusReporterConfig implements MetricsReporterConfigThree {

    private static final Logger log = LoggerFactory.getLogger(PrometheusReporterConfig.class);
    private static final String SERVLET_CLASS = "io.prometheus.client.exporter.MetricsServlet";
    private static final String SENDER_CLASS = "io.prometheus.client.exporter.PushGateway";


    private final List<PrometheusReporter> reporters = new ArrayList<>();
    private MetricRegistry registry;

    public PrometheusReporterConfig() {
        // Hack to support servlet type w/o period failing validation.
        setPeriod(60);
    }

    @Override
    public boolean enable(MetricRegistry registry) {
        this.registry = registry;


        if (getType().equals(Type.servlet)) {
            if (!isClassAvailable(SERVLET_CLASS)) {
                log.error("Tried to enable Prometheus Server Exporter, but class {} was not found", SERVLET_CLASS);
                return false;
            }

            return true;
        }

        // If we reached here then the config is for a pushgateway setup.
        if (!isClassAvailable(SENDER_CLASS)) {
            log.error("Tried to enable Prometheus Reporter, but class {} was not found", SENDER_CLASS);
            return false;
        }

        List<HostPort> hostPorts = getFullHostList();
        if (hostPorts == null || hostPorts.isEmpty()) {
            log.error("Prometheus pushgateway expects at least one host.");
            return false;
        }

        for (HostPort hostPort : hostPorts) {
            MetricFilter filter = MetricFilterTransformer.generateFilter(getPredicate());
            PrometheusReporter reporter = new PrometheusReporter(
                    String.format("%s:%d", hostPort.getHost(), hostPort.getPort()),
                    getJob(),
                    getResolvedLabels(),
                    registry,
                    name,
                    filter,
                    getRealRateunit(),
                    getRealDurationunit()
            );

            reporter.start(getPeriod(), getRealTimeunit());
            reporters.add(reporter);
        }

        return true;
    }

    @Override
    public void report() {
        for (PrometheusReporter reporter : reporters) {
            reporter.report();
        }
    }

    @Override
    public List<HostPort> getFullHostList() {
        return getHostListAndStringList();
    }

    public MetricsServlet getMetricsServlet() {
        CollectorRegistry collectorRegistry = new CollectorRegistry();
        collectorRegistry.register(new MetricRegistryDecorator(registry, job, getResolvedLabels()));
        return new MetricsServlet(collectorRegistry);
    }
}
