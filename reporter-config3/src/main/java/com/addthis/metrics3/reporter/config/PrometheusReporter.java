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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.addthis.metrics3.reporter.config.prometheus.MetricRegistryDecorator;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;

public class PrometheusReporter extends ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(ZabbixReporter.class);

    private final PushGateway pushGateway;
    private final String address;
    private final String job;
    private final CollectorRegistry registry = new CollectorRegistry();

    public PrometheusReporter(String address,
                              String job,
                              Map<String, String> labels,
                              MetricRegistry registry,
                              String name,
                              MetricFilter filter,
                              TimeUnit rateUnit,
                              TimeUnit durationUnit) {

        super(registry, name, filter, rateUnit, durationUnit);

        this.pushGateway = new PushGateway(address);
        this.address = address;
        this.job = job;
        this.registry.register(new MetricRegistryDecorator(registry, job, labels));
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        try {
            this.pushGateway.push(registry, job, new HashMap<String, String>());
        } catch (IOException ex) {
            logger.error("failed to report prometheus metrics to {}", address, ex);
        }
    }
}