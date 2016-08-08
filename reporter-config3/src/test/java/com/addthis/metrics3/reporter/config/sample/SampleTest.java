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

package com.addthis.metrics3.reporter.config.sample;

import com.addthis.metrics3.reporter.config.ReporterConfig;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


// TODO: Make this an integration test
public class SampleTest
{
    private static final Logger log = LoggerFactory.getLogger(SampleTest.class);

    private static final Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));
    private final int loops = 2;

    private void runLoop(ReporterConfig config) throws Exception
    {
        MetricRegistry registry = new MetricRegistry();
        Counter counter = registry.counter("mycounter");
        Meter meter = registry.meter("foo");
        config.enableConsole(registry);
        for (int i=0; i< loops; i++)
        {
            counter.inc();
            meter.mark();
            Thread.sleep(1000);
            log.debug("runLoop tick");
        }
        log.info("Done with sample data loop");
    }

    @Test
    public void sampleConsole() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/console.yaml");
        System.out.println(yaml.dump(config));
        runLoop(config);
    }

    @Test
    public void sampleCSV() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/csv.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample CSV Reporter");
        runLoop(config);
    }

    @Test
    public void sampleGanglia() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/ganglia.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample Ganglia Reporter");
        runLoop(config);

    }

    @Test
    public void sampleGangliaGmond() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/ganglia-gmond.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample Ganglia Gmond");
        runLoop(config);
    }


    @Test
    public void sampleGraphite() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/graphite.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample Graphite");
        runLoop(config);
    }

    @Test
    public void sampleGraphiteString() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/graphite-string.yaml");
        System.out.println(yaml.dump(config));
        log.info("Graphite String");
        runLoop(config);
    }

    @Test
    public void sampleGraphiteStringDupe() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/graphite-string-dupe.yaml");
        System.out.println(yaml.dump(config));
        log.info("Graphite String Dupe");
        runLoop(config);
    }

    @Test
    public void sampleInfluxDB() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/influxdb.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample InfluxDB");
        runLoop(config);
    }

    @Test
    public void sampleStatsD() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/statsd.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample StatsD");
        assertNotNull(config.getStatsd());
        assertEquals(1, config.getStatsd().size());
        runLoop(config);
    }

    @Test
    public void sampleStatsDMulti() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/statsd-multi.yaml");
        System.out.println(yaml.dump(config));
        log.info("StatsD Multi");
        assertNotNull(config.getStatsd());
        assertEquals(2, config.getStatsd().size());
        runLoop(config);
    }

    @Test
    public void sampleMulti() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/multi.yaml");
        System.out.println(yaml.dump(config));
        log.info("Multi Reporter");
        runLoop(config);
    }

}
