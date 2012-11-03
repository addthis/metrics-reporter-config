package com.addthis.metrics.reporter.config;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PredicateConfigTest
{


    // Invalid by validation
    @Test
    public void empty()
    {
        PredicateConfig pc = new PredicateConfig("white", new ArrayList());
        assertFalse(pc.allowString("foo"));
        pc = new PredicateConfig("black", new ArrayList());
        assertTrue(pc.allowString("foo"));
    }

    @Test
    public void singleBlack()
    {
        PredicateConfig pc = new PredicateConfig("black", ImmutableList.of("^bad.+"));
        assertTrue(pc.allowString("foo"));
        assertTrue(pc.allowString("foobad"));
        assertFalse(pc.allowString("badFoo"));
    }

    @Test
    public void jmxRegex()
    {
        PredicateConfig pc = new PredicateConfig("black", ImmutableList.of(".*JMXONLY$"));
        assertTrue(pc.allowString("foo"));
        assertTrue(pc.allowString("foobad"));
        assertTrue(pc.allowString("badFoo"));
        assertTrue(pc.allowString("com.example.foo.CoolMetric"));
        assertFalse(pc.allowString("com.example.foo.CoolMetric_JMXONLY"));
    }

    @Test
    public void singleWhite()
    {
        PredicateConfig pc = new PredicateConfig("white", ImmutableList.of("^good.+"));
        assertFalse(pc.allowString("foo"));
        assertFalse(pc.allowString("foogood"));
        assertTrue(pc.allowString("goodFoo"));
    }

    @Test
    public void multiBlack()
    {
        PredicateConfig pc = new PredicateConfig("black", ImmutableList.of("^bad.+", ".*bad$"));
        assertTrue(pc.allowString("foo"));
        assertTrue(pc.allowString("foobadfoo"));
        assertFalse(pc.allowString("badFoo"));
        assertFalse(pc.allowString("foobad"));
    }

    @Test
    public void multiWhite()
    {
        PredicateConfig pc = new PredicateConfig("white", ImmutableList.of("^good.+", ".*good$"));
        assertFalse(pc.allowString("foo"));
        assertFalse(pc.allowString("foobadfoo"));
        assertFalse(pc.allowString("foogoodfoo"));
        assertTrue(pc.allowString("goodFoo"));
        assertTrue(pc.allowString("foogood"));
        assertTrue(pc.allowString("goodfoogood"));
    }

}
