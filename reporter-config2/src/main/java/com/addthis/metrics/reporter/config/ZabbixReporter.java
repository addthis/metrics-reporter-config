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

package com.addthis.metrics.reporter.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;
import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.SenderResult;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

public class ZabbixReporter extends AbstractPollingReporter implements MetricProcessor<List<DataObject>>
{
    private static final Logger log = LoggerFactory.getLogger(ZabbixReporter.class);
    private final ZabbixSender sender;
    private final String hostName;
    private final String prefix;
    private final double durationFactor;
    private final double rateFactor;
    private final MetricPredicate predicate;
    private final Clock clock;
    private long startTime;

    public ZabbixReporter(ZabbixSender sender, String hostName, String prefix,
                          String name, TimeUnit rateUnit, TimeUnit durationUnit,
                          MetricPredicate predicate, Clock clock)
    {
        super(Metrics.defaultRegistry(), name);

        this.rateFactor = rateUnit.toSeconds(1);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);

        this.sender = sender;
        this.hostName = hostName;
        this.prefix = prefix;


        this.predicate = predicate;
        this.clock = clock;
    }

    private DataObject toDataObject(MetricName key, String suffix, Object value) {
        return DataObject.builder().host(hostName).key(prefix + key + suffix).value("" + value).build();
    }

    public void processGauge(MetricName name, Gauge<?> gauge, List<DataObject> dataObjectList) throws Exception
    {
        dataObjectList.add(toDataObject(name, "", gauge.value()));
    }

    @Override
    public void processTimer(MetricName name, Timer timer, List<DataObject> dataObjectList) throws Exception
    {
        addMeterDataObject(name, timer, dataObjectList);
        addSnapshotDataObjectWithConvertDuration(name, timer, dataObjectList);
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, List<DataObject> dataObjectList) throws Exception
    {
        addSnapshotDataObject(name, histogram, dataObjectList);
    }

    @Override
    public void processCounter(MetricName name, Counter counter, List<DataObject> dataObjectList) throws Exception
    {
        dataObjectList.add(toDataObject(name, "", counter.count()));
    }

    @Override
    public void processMeter(MetricName name, Metered meter, List<DataObject> dataObjectList) throws Exception
    {
        addMeterDataObject(name, meter, dataObjectList);
    }

    private void addSnapshotDataObject(MetricName key, Histogram histogram, List<DataObject> dataObjectList) {
        Snapshot snapshot = histogram.getSnapshot();
        dataObjectList.add(toDataObject(key, ".min", histogram.min()));
        dataObjectList.add(toDataObject(key, ".max", histogram.max()));
        dataObjectList.add(toDataObject(key, ".mean", histogram.mean()));
        dataObjectList.add(toDataObject(key, ".stddev", histogram.stdDev()));
        dataObjectList.add(toDataObject(key, ".median", snapshot.getMedian()));
        dataObjectList.add(toDataObject(key, ".75th", snapshot.get75thPercentile()));
        dataObjectList.add(toDataObject(key, ".95th", snapshot.get95thPercentile()));
        dataObjectList.add(toDataObject(key, ".98th", snapshot.get98thPercentile()));
        dataObjectList.add(toDataObject(key, ".99th", snapshot.get99thPercentile()));
        dataObjectList.add(toDataObject(key, ".99.9th", snapshot.get999thPercentile()));
    }

    private void addSnapshotDataObjectWithConvertDuration(MetricName key, Timer timer, List<DataObject> dataObjectList) {
        Snapshot snapshot = timer.getSnapshot();
        dataObjectList.add(toDataObject(key, ".min", convertDuration(timer.min())));
        dataObjectList.add(toDataObject(key, ".max", convertDuration(timer.max())));
        dataObjectList.add(toDataObject(key, ".mean", convertDuration(timer.mean())));
        dataObjectList.add(toDataObject(key, ".stddev", convertDuration(timer.stdDev())));
        dataObjectList.add(toDataObject(key, ".median", convertDuration(snapshot.getMedian())));
        dataObjectList.add(toDataObject(key, ".75th", convertDuration(snapshot.get75thPercentile())));
        dataObjectList.add(toDataObject(key, ".95th", convertDuration(snapshot.get95thPercentile())));
        dataObjectList.add(toDataObject(key, ".98th", convertDuration(snapshot.get98thPercentile())));
        dataObjectList.add(toDataObject(key, ".99th", convertDuration(snapshot.get99thPercentile())));
        dataObjectList.add(toDataObject(key, ".99.9th", convertDuration(snapshot.get999thPercentile())));
    }

    private void addMeterDataObject(MetricName key, Metered meter, List<DataObject> dataObjectList) {
        dataObjectList.add(toDataObject(key, ".count", meter.count()));
        dataObjectList.add(toDataObject(key, ".meanRate", convertRate(meter.meanRate())));
        dataObjectList.add(toDataObject(key, ".1-minuteRate", convertRate(meter.oneMinuteRate())));
        dataObjectList.add(toDataObject(key, ".5-minuteRate", convertRate(meter.fiveMinuteRate())));
        dataObjectList.add(toDataObject(key, ".15-minuteRate", convertRate(meter.fifteenMinuteRate())));
    }

    private double convertDuration(double duration) {
        return duration * durationFactor;
    }

    private double convertRate(double rate) {
        return rate * rateFactor;
    }

    @Override
    public void start(long period, TimeUnit unit) {
        this.startTime = clock.time();
        super.start(period, unit);
    }

    @Override
    public void run() {
        final Set<Map.Entry<MetricName, Metric>> metrics = getMetricsRegistry().allMetrics().entrySet();
        try {
            List<DataObject> dataObjectList = new ArrayList<DataObject>();
            for (Map.Entry<MetricName, Metric> entry : metrics) {
                final MetricName metricName = entry.getKey();
                final Metric metric = entry.getValue();
                if (predicate.matches(metricName, metric)) {
                    metric.processWith(this, entry.getKey(), dataObjectList);
                }
            }

            SenderResult senderResult = sender.send(dataObjectList);
            if (!senderResult.success()) {
                log.warn("metrics reporting to zabbix {} unsuccessful: {}", sender.getHost(), sender.getPort(), senderResult);
            } else if (log.isDebugEnabled()) {
                log.debug("metrics reported to zabbix {} {}: {}", sender.getHost(), sender.getPort(), senderResult);
            }
        } catch (Exception e) {
            log.error("failed to report metrics to " + sender.getHost() + ':' + sender.getPort(), e);
        }
    }
}
