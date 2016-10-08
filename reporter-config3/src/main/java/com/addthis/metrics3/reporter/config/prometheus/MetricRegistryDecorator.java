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

package com.addthis.metrics3.reporter.config.prometheus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

public class MetricRegistryDecorator extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(MetricRegistryDecorator.class);

    private final MetricRegistry registry;
    private final List<String> labelNames = new ArrayList<>();
    private final List<String> labelValues = new ArrayList<>();

    public MetricRegistryDecorator(MetricRegistry registry, String job, Map<String, String> labels) {
        this.registry = registry;

        labelNames.add("job");
        labelValues.add(job);

        for (Map.Entry<String, String> entry : labels.entrySet()) {
            labelNames.add(entry.getKey());
            labelValues.add(entry.getValue());
        }
    }

    private static String getHelpMessage(String metricName, Metric metric) {
        return String.format("Generated from dropwizard metric import (metric=%s, type=%s)", metricName, metric.getClass().getName());
    }

    public MetricFamilySamples fromCounter(String name, Counter counter) {
        Sample sample = new Sample(name, labelNames, labelValues, new Long(counter.getCount()).doubleValue());
        return new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(name, counter), Arrays.asList(sample));
    }

    public MetricFamilySamples fromGauge(String name, Gauge gauge) {
        Object obj = gauge.getValue();

        List<String> labelNames = new ArrayList<>(this.labelNames);
        List<String> labelValues = new ArrayList<>(this.labelValues);

        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            value = ((Boolean) obj) ? 1 : 0;
        } else if (obj instanceof String) {
            // Support string gauge constants by setting gauge_value label to the value
            // with a number value of 1.
            labelNames.add("gauge_value");
            labelValues.add((String) obj);
            value = 1;
        } else {
            return null;
        }

        Sample sample = new Sample(name, labelNames, labelValues, value);
        return new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(name, gauge), Arrays.asList(sample));
    }

    public MetricFamilySamples fromHistogram(String name, Histogram histogram) {
        return fromSnapshotAndCount(name, histogram.getSnapshot(), histogram.getCount(), 1.0, getHelpMessage(name, histogram));
    }

    public MetricFamilySamples fromTimer(String name, Timer timer) {
        return fromSnapshotAndCount(name, timer.getSnapshot(), timer.getCount(), 1.0D / TimeUnit.SECONDS.toNanos(1L), getHelpMessage(name, timer));
    }

    public MetricFamilySamples fromMeter(String name, Meter meter) {
        List<Sample> samples = Arrays.asList(new Sample(name + "_total", labelNames, labelValues, meter.getCount()));
        return new MetricFamilySamples(name + "_total", Type.COUNTER, getHelpMessage(name, meter), samples);
    }

    public MetricFamilySamples fromSnapshotAndCount(String name, Snapshot snapshot, long count, double factor, String helpMessage) {
        List<String> labelNames = addToEnd(this.labelNames, "quantile");

        List<Sample> samples = Arrays.asList(
                new Sample(name, labelNames, addToEnd(labelValues, "0.5"), snapshot.getMedian() * factor),
                new Sample(name, labelNames, addToEnd(labelValues, "0.75"), snapshot.get75thPercentile() * factor),
                new Sample(name, labelNames, addToEnd(labelValues, "0.95"), snapshot.get95thPercentile() * factor),
                new Sample(name, labelNames, addToEnd(labelValues, "0.98"), snapshot.get98thPercentile() * factor),
                new Sample(name, labelNames, addToEnd(labelValues, "0.99"), snapshot.get99thPercentile() * factor),
                new Sample(name, labelNames, addToEnd(labelValues, "0.999"), snapshot.get999thPercentile() * factor),
                new Sample(name + "_count", new ArrayList<String>(), new ArrayList<String>(), count)
        );

        return new MetricFamilySamples(name, Type.SUMMARY, helpMessage, samples);
    }

    private static List<String> addToEnd(List<String> list, String value) {
        List<String> copy = new ArrayList<>(list);
        copy.add(value);
        return copy;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> metricFamilySamples = new ArrayList<>();

        for (SortedMap.Entry<String, Gauge> entry : registry.getGauges().entrySet()) {
            MetricFamilySamples sample = fromGauge(sanitizeMetricName(entry.getKey()), entry.getValue());
            if (sample != null) {
                metricFamilySamples.add(sample);
            }
        }

        for (SortedMap.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
            MetricFamilySamples sample = fromCounter(sanitizeMetricName(entry.getKey()), entry.getValue());
            if (sample != null) {
                metricFamilySamples.add(sample);
            }
        }

        for (SortedMap.Entry<String, Histogram> entry : registry.getHistograms().entrySet()) {
            MetricFamilySamples sample = fromHistogram(sanitizeMetricName(entry.getKey()), entry.getValue());
            if (sample != null) {
                metricFamilySamples.add(sample);
            }
        }

        for (SortedMap.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            MetricFamilySamples sample = fromTimer(sanitizeMetricName(entry.getKey()), entry.getValue());
            if (sample != null) {
                metricFamilySamples.add(sample);
            }
        }

        for (SortedMap.Entry<String, Meter> entry : registry.getMeters().entrySet()) {
            MetricFamilySamples sample = fromMeter(sanitizeMetricName(entry.getKey()), entry.getValue());
            if (sample != null) {
                metricFamilySamples.add(sample);
            }
        }

        return metricFamilySamples;
    }

}
