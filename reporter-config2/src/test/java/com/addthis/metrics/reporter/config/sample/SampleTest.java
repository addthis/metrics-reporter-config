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

import java.util.concurrent.TimeUnit;

import com.addthis.metrics.reporter.config.CsvReporterConfig;
import com.addthis.metrics.reporter.config.ReporterConfig;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.junit.Assert.assertEquals;


// TODO: Make this an integration test
public class SampleTest
{
    private static final Logger log = LoggerFactory.getLogger(SampleTest.class);

    private static final Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));
    private final int loops = 2;

    private void runLoop(ReporterConfig config) throws Exception
    {
        Counter counter = Metrics.newCounter(getClass(), "counter");
        Meter meter = Metrics.newMeter(getClass(), "meter", "foo", TimeUnit.SECONDS);
        config.enableConsole();
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
    public void sampleStatsD() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/statsd.yaml");
        System.out.println(yaml.dump(config));
        log.info("Sample StatsD");
        runLoop(config);
    }

    @Test
    public void sampleStatsDMulti() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/statsd-multi.yaml");
        System.out.println(yaml.dump(config));
        log.info("StatsD Multi");
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

    @Test
    public void sampleGeneric() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/generic.yaml");
        config.enableAll();
        System.out.println(yaml.dump(config));
        assertEquals(1, config.getReporters().size());
        assertEquals(CsvReporterConfig.class, config.getReporters().iterator().next().getClass());
        log.info("Sample generic Reporter");
        runLoop(config);
    }

}
