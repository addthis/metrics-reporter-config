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

package com.addthis.metrics.reporter.config.prometheus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.addthis.metrics.reporter.config.AbstractPrometheusReporterConfig;
import com.addthis.metrics.reporter.config.MetricsReporterConfigTwo;
import com.yammer.metrics.Metrics;

public class PrometheusReporterConfig extends AbstractPrometheusReporterConfig implements MetricsReporterConfigTwo
{
    private static final Logger log = LoggerFactory.getLogger(PrometheusReporterConfig.class);

    private PrometheusReporter reporter;

    @Override
    public boolean enable()
    {
        boolean success = setup("com.addthis.metrics.reporter.config.prometheus.PrometheusReporter") &&
                          setup("com.google.protobuf.ProtocolMessageEnum") &&
                          setup("io.prometheus.client.Collector");
        if (!success)
        {
            return false;
        }

        try
        {
            enableMetrics3();
        }
        catch (Exception e)
        {
            log.error("Faliure while enabling PrometheusReporter", e);
            return false;
        }

        return true;
    }

    private void enableMetrics3()
    {
        reporter = new PrometheusReporter(Metrics.defaultRegistry(), this);
    }

    private boolean setup(String className)
    {
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable PrometheusReporter, but class {} was not found", className);
            return false;
        }
        return true;
    }

    /**
     * Stop the metrics exporter.
     */
    public void stop() {
        if (reporter != null)
        {
            reporter.stop();
        }
    }

    public PrometheusReporter getReporter()
    {
        return reporter;
    }
}
