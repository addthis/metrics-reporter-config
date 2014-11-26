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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.InetSocketAddress;

import java.util.List;

import com.addthis.metrics.reporter.config.AbstractGangliaReporterConfig;
import com.addthis.metrics.reporter.config.HostPort;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bug in jackson-databind will attempt to load this class even when no ganglia
 * reporter configurations have been specified. We must use reflection to interact
 * with the optional libraries. Otherwise we receive ClassNotFoundExceptions.
 */
public class GangliaReporterConfig extends AbstractGangliaReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(GangliaReporterConfig.class);

    private static void setPrivateField(Class clazz, Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException
    {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void enableMetrics3(HostPort hostPort, MetricRegistry registry)
    {
        try
        {
            Class gmetricClass = Class.forName("info.ganglia.gmetric4j.gmetric.GMetric");
            Class gmetricEnum = Class.forName("info.ganglia.gmetric4j.gmetric.GMetric$UDPAddressingMode");
            Class gangliaReporterClass = Class.forName("com.codahale.metrics.ganglia.GangliaReporter");
            Class builderClass = Class.forName("com.codahale.metrics.ganglia.GangliaReporter$Builder");
            Method registryMethod = gangliaReporterClass.getMethod("forRegistry", MetricRegistry.class);
            Method buildMethod = builderClass.getMethod("build", gmetricClass);
            Object builder = registryMethod.invoke(null, registry);
            Object gmetric = gmetricClass.getConstructor(String.class, int.class, gmetricEnum, int.class)
                                         .newInstance(hostPort.getHost(), hostPort.getHost(),
                                                      Enum.valueOf(gmetricEnum, "UNICAST"), 1);
            setPrivateField(builderClass, builder, "rateUnit", getRealRateunit());
            setPrivateField(builderClass, builder, "durationUnit", getRealDurationunit());
            setPrivateField(builderClass, builder, "prefix", getResolvedPrefix());
            setPrivateField(builderClass, builder, "filter", MetricFilterTransformer.generateFilter(getPredicate()));
            ScheduledReporter reporter = (ScheduledReporter) buildMethod.invoke(builder, gmetric);
            reporter.start(getPeriod(), getRealTimeunit());
        }
        catch (Exception e)
        {
            log.error("Failed to enable GangliaReporter", e);
        }
    }

    @Override
    public boolean enable(MetricRegistry registry)
    {
        boolean success = setup("com.codahale.metrics.ganglia.GangliaReporter");
        if (!success)
        {
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        for (HostPort hostPort : hosts)
        {
            log.info("Enabling GangliaReporter to {}:{}", new Object[]{hostPort.getHost(), hostPort.getPort()});
            try
            {
                enableMetrics3(hostPort, registry);
            }
            catch (Exception e)
            {
                log.error("Faliure while enabling GangliaReporter", e);
                return false;
            }
        }
        return true;
    }

}
