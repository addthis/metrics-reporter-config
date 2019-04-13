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

import java.util.List;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringJoiner;

import com.codahale.metrics.MetricRegistry;
import com.addthis.metrics.reporter.config.HostPort;
import com.addthis.metrics.reporter.config.AbstractKafkaReporterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hengyunabc.metrics.KafkaReporter;
import kafka.producer.ProducerConfig;


public class KafkaReporterConfig extends AbstractKafkaReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(KafkaReporterConfig.class);

    private MetricRegistry registry;

    private KafkaReporter reporter;

    private boolean checkClass(String className) {
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable KafkaReporter, but class {} was not found", className);
            return false;
        } else
            {
            return true;
        }
    }

    @Override
    public boolean enable(MetricRegistry registry) {
        this.registry = registry;

        boolean success = checkClass("com.addthis.metrics.reporter.config.AbstractKafkaReporterConfig");
        if (!success)
        {
            return false;
        }

        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable KafkaReporter");
            return false;
        }

        log.info("Enabling KafkaReporter to {}", "");
        try
        {
            StringJoiner brokerList = new StringJoiner(",");
            for (HostPort host : getFullHostList()) {
                brokerList.add(host.toString());
            }

            Properties props = new Properties();
            props.put("metadata.broker.list", brokerList.toString());
            props.put("serializer.class", getSerializer());
            props.put("partitioner.class", getPartitioner());
            props.put("request.required.acks", getRequiredAcks());
            ProducerConfig config = new ProducerConfig(props);

            reporter = KafkaReporter.forRegistry(registry)
                       .config(config)
                       .topic(getTopic())
                       .hostName(getHostname())
                       .ip(getIp())
                       .labels(getResolvedLabels())
                       .prefix(getResolvedPrefix())
                       .filter(MetricFilterTransformer.generateFilter(getPredicate()))
                       .build();

            reporter.start(getPeriod(), getRealTimeunit());
        }
        catch (Exception e)
        {
            log.error("Failure while Enabling KafkaReporter", e);
            return false;
        }
        return true;
    }

    @Override
    public void report() {
        if (reporter != null) {
            reporter.report();
        }
    }

}
