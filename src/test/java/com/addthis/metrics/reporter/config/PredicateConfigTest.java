/*
 * Copyright (C) 2012 AddThis
 *
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

   @Test
   public void cassWhite()
    {
        PredicateConfig pc = new PredicateConfig("white",
                                                 ImmutableList.of("^org.apache.cassandra.metrics.ColumnFamily.system.*",
                                                                  "^org.apache.cassandra.metrics.Cache.*"),
                                                 true);
        assertFalse(pc.allowString("foo"));
        assertFalse(pc.allowString("org.apache.cassandra.metrics.StuffIMadeUp"));
        assertFalse(pc.allowString("org.apache.cassandra.metrics.ColumnFamily.Keyspace1.Counter3.LiveSSTableCount"));
        assertTrue(pc.allowString("org.apache.cassandra.metrics.Cache.KeyCache.Size"));
        assertTrue(pc.allowString("org.apache.cassandra.metrics.ColumnFamily.system.NodeIdInfo.MeanRowSize"));
    }


   @Test
   public void cassSampleDefault()
    {
        PredicateConfig pc = new PredicateConfig("white",
                                                 ImmutableList.of("^org.apache.cassandra.metrics.Cache.+",
                                                                  "^org.apache.cassandra.metrics.ClientRequest.+",// includes ClientRequestMetrics
                                                                  "^org.apache.cassandra.metrics.CommitLog.+",
                                                                  "^org.apache.cassandra.metrics.Compaction.+",
                                                                  "^org.apache.cassandra.metrics.DroppedMetrics.+",
                                                                  "^org.apache.cassandra.metrics.ReadRepair.+",
                                                                  "^org.apache.cassandra.metrics.Storage.+",
                                                                  "^org.apache.cassandra.metrics.ThradPools.+"),
                                                 true);
        assertFalse(pc.allowString("foo"));
        assertFalse(pc.allowString("org.apache.cassandra.metrics.StuffIMadeUp"));
        assertFalse(pc.allowString("org.apache.cassandra.metrics.ColumnFamily.Keyspace1.Counter3.LiveSSTableCount"));
        assertTrue(pc.allowString("org.apache.cassandra.metrics.Cache.KeyCache.Size"));
        assertFalse(pc.allowString("org.apache.cassandra.metrics.ColumnFamily.system.NodeIdInfo.MeanRowSize"));
    }


}
