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

package com.addthis.metrics.reporter.config.sample;

import com.addthis.metrics.reporter.config.ReporterConfig;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


// TODO: Make this an integration test
public class NameTest
{
    private static final Logger log = LoggerFactory.getLogger(NameTest.class);

    private static final Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));

    // Without mocking up a full repoter & registry it's surprisingly
    // difficult to get the MetricName of a metric, or trace the
    // application of predicates.  This is a hacky test for visual
    // inspection.
    @Test
    public void csvNamePrinting() throws Exception
    {
        log.debug("name test");
        Counter counter = Metrics.newCounter(getClass(), "mycounter");
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/csv-predicate.yaml");
        config.enableAll();
        counter.inc();
        Thread.sleep(10000);
    }

}
