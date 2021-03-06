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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatsDReporterConfigTest {
    private static final HostPort testHostPort1 = new HostPort("test-host1", 1234);
    private static final HostPort testHostPort2 = new HostPort("test-host2", 2345);

    private static final TimeUnit testRateunit = TimeUnit.DAYS;
    private static final TimeUnit testDurationunit = TimeUnit.MICROSECONDS;
    private static final TimeUnit testTimeunit = TimeUnit.HOURS;
    private static final String testBadTimeunit = "BAD_UNIT";
    private static final PredicateConfig testPredicate = new PredicateConfig("white", Arrays.asList(".*test_pattern.*"), true);
    private static final long testPeriod = 321L;
    private static final String testPrefix = "test_prefix";

    @Test
    public void startEmptyHosts() {
        StatsDReporterConfig config = new StatsDReporterConfig();
        assertFalse(config.enable());
        config.stopForTests();
    }

    @Test
    public void startOneHost() {
        StatsDReporterConfig config = buildConfig(
            Arrays.asList(testHostPort1),
            testTimeunit.toString());
        assertTrue(config.enable());
        config.stopForTests();
    }

    @Test
    public void startOneHostBadField() {
        StatsDReporterConfig config = buildConfig(
            Arrays.asList(testHostPort1),
            testBadTimeunit);
        assertFalse(config.enable());
        config.stopForTests();
    }

    @Test
    public void startManyHosts() {
        StatsDReporterConfig config = buildConfig(
            Arrays.asList(testHostPort1, testHostPort2),
            testTimeunit.toString());
        assertTrue(config.enable());
        config.stopForTests();
    }

    @Test
    public void startManyHostsBadField() {
        StatsDReporterConfig config = buildConfig(
            Arrays.asList(testHostPort1, testHostPort2),
            testBadTimeunit);
        assertFalse(config.enable());
        config.stopForTests();
    }

    private static StatsDReporterConfig buildConfig(List<HostPort> hosts, String timeUnit) {
        StatsDReporterConfig reporter = new StatsDReporterConfig();

        reporter.setHosts(hosts);

        reporter.setRateunit(testRateunit.toString());
        reporter.setDurationunit(testDurationunit.toString());
        reporter.setPrefix(testPrefix);
        reporter.setPredicate(testPredicate);
        reporter.setPeriod(testPeriod);
        reporter.setTimeunit(timeUnit);

        return reporter;
    }
}
