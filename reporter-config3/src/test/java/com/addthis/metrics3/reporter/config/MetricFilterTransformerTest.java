package com.addthis.metrics3.reporter.config;

import com.addthis.metrics.reporter.config.PredicateConfig;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetricFilterTransformerTest {

    private static final Yaml yaml = new Yaml(new Constructor(PredicateConfig.class));

    @Test
    public void testBlacklist() {
        ClassLoader classLoader = getClass().getClassLoader();
        PredicateConfig config = (PredicateConfig) yaml.load(classLoader.getResourceAsStream("predicate-black.yaml"));

        MetricFilter filter = MetricFilterTransformer.generateFilter(config);

        assertFalse(filter.matches("org.eclipse.jetty.server.foo.bar", null));
        assertTrue(filter.matches("com.addthis.timer.m1_rate", null));

        Meter meter = new Meter();
        assertTrue(filter.matches("com.addthis.site.clients.m1_rate", meter));
        assertFalse(filter.matches("com.addthis.site.clients.m5_rate", meter));
        assertFalse(filter.matches("com.addthis.site.clients.m15_rate", meter));
        assertFalse(filter.matches("com.addthis.site.clients.mean_rate", meter));
    }

    @Test
    public void testWhitelist() {
        ClassLoader classLoader = getClass().getClassLoader();
        PredicateConfig config = (PredicateConfig) yaml.load(classLoader.getResourceAsStream("predicate-white.yaml"));

        MetricFilter filter = MetricFilterTransformer.generateFilter(config);

        assertTrue(filter.matches("org.eclipse.jetty.server.foo.bar", null));
        assertFalse(filter.matches("com.addthis.timer.m1_rate", null));

        Meter meter = new Meter();
        assertFalse(filter.matches("org.eclipse.jetty.server.m1_rate", meter));
        assertTrue(filter.matches("org.eclipse.jetty.server.m5_rate", meter));
        assertTrue(filter.matches("org.eclipse.jetty.server.m15_rate", meter));
        assertTrue(filter.matches("org.eclipse.jetty.server.mean_rate", meter));
    }
}
