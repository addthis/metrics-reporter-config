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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.SenderResult;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

public class ZabbixReporter extends ScheduledReporter
{
    private static final Logger log = LoggerFactory.getLogger(ZabbixReporter.class);
    private final ZabbixSender sender;
    private final String hostName;
    private final String prefix;

    public ZabbixReporter(ZabbixSender sender, String hostName, String prefix,
                          MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit)
    {
        super(registry, name, filter, rateUnit, durationUnit);
        this.sender = sender;
        this.hostName = hostName;
        this.prefix = prefix;
    }

    private DataObject toDataObject(String key, String suffix, Object value) {
        return DataObject.builder().host(hostName).key(prefix + key + suffix).value("" + value).build();
    }

    private void addSnapshotDataObject(String key, Snapshot snapshot, List<DataObject> dataObjectList) {
        dataObjectList.add(toDataObject(key, ".min", snapshot.getMin()));
        dataObjectList.add(toDataObject(key, ".max", snapshot.getMax()));
        dataObjectList.add(toDataObject(key, ".mean", snapshot.getMean()));
        dataObjectList.add(toDataObject(key, ".stddev", snapshot.getStdDev()));
        dataObjectList.add(toDataObject(key, ".median", snapshot.getMedian()));
        dataObjectList.add(toDataObject(key, ".75th", snapshot.get75thPercentile()));
        dataObjectList.add(toDataObject(key, ".95th", snapshot.get95thPercentile()));
        dataObjectList.add(toDataObject(key, ".98th", snapshot.get98thPercentile()));
        dataObjectList.add(toDataObject(key, ".99th", snapshot.get99thPercentile()));
        dataObjectList.add(toDataObject(key, ".99.9th", snapshot.get999thPercentile()));
    }

    private void addSnapshotDataObjectWithConvertDuration(String key, Snapshot snapshot, List<DataObject> dataObjectList) {
        dataObjectList.add(toDataObject(key, ".min", convertDuration(snapshot.getMin())));
        dataObjectList.add(toDataObject(key, ".max", convertDuration(snapshot.getMax())));
        dataObjectList.add(toDataObject(key, ".mean", convertDuration(snapshot.getMean())));
        dataObjectList.add(toDataObject(key, ".stddev", convertDuration(snapshot.getStdDev())));
        dataObjectList.add(toDataObject(key, ".median", convertDuration(snapshot.getMedian())));
        dataObjectList.add(toDataObject(key, ".75th", convertDuration(snapshot.get75thPercentile())));
        dataObjectList.add(toDataObject(key, ".95th", convertDuration(snapshot.get95thPercentile())));
        dataObjectList.add(toDataObject(key, ".98th", convertDuration(snapshot.get98thPercentile())));
        dataObjectList.add(toDataObject(key, ".99th", convertDuration(snapshot.get99thPercentile())));
        dataObjectList.add(toDataObject(key, ".99.9th", convertDuration(snapshot.get999thPercentile())));
    }

    private void addMeterDataObject(String key, Metered meter, List<DataObject> dataObjectList) {
        dataObjectList.add(toDataObject(key, ".count", meter.getCount()));
        dataObjectList.add(toDataObject(key, ".meanRate", convertRate(meter.getMeanRate())));
        dataObjectList.add(toDataObject(key, ".1-minuteRate", convertRate(meter.getOneMinuteRate())));
        dataObjectList.add(toDataObject(key, ".5-minuteRate", convertRate(meter.getFiveMinuteRate())));
        dataObjectList.add(toDataObject(key, ".15-minuteRate", convertRate(meter.getFifteenMinuteRate())));
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        List<DataObject> dataObjectList = new ArrayList<DataObject>();
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            DataObject dataObject = DataObject.builder().host(hostName).key(prefix + entry.getKey())
                                              .value(entry.getValue().getValue().toString()).build();
            dataObjectList.add(dataObject);
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            DataObject dataObject = DataObject.builder().host(hostName).key(prefix + entry.getKey())
                                              .value("" + entry.getValue().getCount()).build();
            dataObjectList.add(dataObject);
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            Histogram histogram = entry.getValue();
            Snapshot snapshot = histogram.getSnapshot();
            addSnapshotDataObject(entry.getKey(), snapshot, dataObjectList);
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            Meter meter = entry.getValue();
            addMeterDataObject(entry.getKey(), meter, dataObjectList);
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            Timer timer = entry.getValue();
            addMeterDataObject(entry.getKey(), timer, dataObjectList);
            addSnapshotDataObjectWithConvertDuration(entry.getKey(), timer.getSnapshot(), dataObjectList);
        }

        try {
            SenderResult senderResult = sender.send(dataObjectList);
            if (!senderResult.success()) {
                log.warn("metrics reporting to zabbix {} unsuccessful: {}", sender.getHost(), sender.getPort(), senderResult);
            } else if (log.isDebugEnabled()) {
                log.debug("metrics reported to zabbix {} {}: {}", sender.getHost(), sender.getPort(), senderResult);
            }
        } catch (IOException e) {
            log.error("failed to report metrics to " + sender.getHost() + ':' + sender.getPort(), e);
        }
    }
}
