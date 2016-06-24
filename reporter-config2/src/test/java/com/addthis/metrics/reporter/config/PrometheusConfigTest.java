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

import java.net.URL;

import org.junit.Test;

import com.addthis.metrics.reporter.config.prometheus.PrometheusReporterConfig;

public class PrometheusConfigTest
{
    @Test
    public void parseCassandraConfig() throws Exception
    {
        URL url = PrometheusConfigTest.class.getResource("/prometheus/prometheus-cassandra.yaml");
        String file = url.getFile();
        ReporterConfig config = ReporterConfig.loadFromFile(file);
        try
        {
            for (PrometheusReporterConfig prometheusReporterConfig : config.getPrometheus())
            {
                // bind to a random port
                prometheusReporterConfig.setPort(0);
            }

            config.enableAll();
        }
        finally
        {
            for (PrometheusReporterConfig prometheusReporterConfig : config.getPrometheus())
            {
                prometheusReporterConfig.stop();
            }
        }
    }
}
