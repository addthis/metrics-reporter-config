package com.addthis.metrics.reporter.sample;

import com.addthis.metrics.reporter.config.ReporterConfig;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class SampleTest
{
    private static final Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));
    private final int loops = 2;

    @Test
    public void sampleConsole() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/console.yaml");
        System.out.println(yaml.dump(config));
        Counter counter = Metrics.newCounter(getClass(), "counter");
        Meter meter = Metrics.newMeter(getClass(), "meter", "foo", TimeUnit.SECONDS);
        config.enableConsole();
        for (int i=0; i< loops; i++)
        {
            counter.inc();
            meter.mark();
            Thread.sleep(1000);
            System.out.println("Hi");
        }
        System.out.println("Done!");
    }

    @Test
    public void sampleCSV() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/csv.yaml");
        System.out.println(yaml.dump(config));
        System.out.println("CSV!!!!!!!!!!");
        Counter counter = Metrics.newCounter(getClass(), "counter");
        Meter meter = Metrics.newMeter(getClass(), "meter", "foo", TimeUnit.SECONDS);
        config.enableCsv();
        for (int i=0; i< loops; i++)
        {
            counter.inc();
            meter.mark();
            Thread.sleep(1000);
        }
        System.out.println("Done!");
    }

    @Test
    public void sampleGanglia() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/ganglia.yaml");
        System.out.println(yaml.dump(config));
        System.out.println("GANGLIA!!!!!!!!!!");
        Counter counter = Metrics.newCounter(getClass(), "counter");
        Meter meter = Metrics.newMeter(getClass(), "meter", "foo", TimeUnit.SECONDS);
        config.enableGanglia();
        for (int i=0; i< loops; i++)
        {
            counter.inc();
            meter.mark();
            Thread.sleep(1000);
        }
        System.out.println("Done!");
    }

    @Test
    public void sampleGraphite() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/graphite.yaml");
        System.out.println(yaml.dump(config));
        System.out.println("GRAPHITE!!!!!!!!!!");
        Counter counter = Metrics.newCounter(getClass(), "counter");
        Meter meter = Metrics.newMeter(getClass(), "meter", "foo", TimeUnit.SECONDS);
        config.enableGraphite();
        for (int i=0; i< loops; i++)
        {
            counter.inc();
            meter.mark();
            Thread.sleep(1000);
        }
        System.out.println("Done!");
    }


    @Test
    public void sampleMulti() throws Exception
    {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/multi.yaml");
        System.out.println(yaml.dump(config));
        System.out.println("MULTI!!!!!!!!!!");
        Counter counter = Metrics.newCounter(getClass(), "counter");
        Meter meter = Metrics.newMeter(getClass(), "meter", "foo", TimeUnit.SECONDS);
        config.enableAll();
        for (int i=0; i< loops; i++)
        {
            counter.inc();
            meter.mark();
            Thread.sleep(1000);
        }
        System.out.println("Done!");
    }

}
