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

import java.io.PrintStream;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsoleReporterConfig extends AbstractConsoleReporterConfig implements MetricsReporterConfigThree
{
    private static final Logger log = LoggerFactory.getLogger(ConsoleReporterConfig.class);

    @Override
    public boolean enable(MetricRegistry registry)
    {
        try
        {
            PrintStream stream = createPrintStream();

            final ConsoleReporter reporter =
                    ConsoleReporter.forRegistry(registry)
                            .convertRatesTo(getRealRateunit())
                            .convertDurationsTo(getRealDurationunit())
                            .filter(MetricFilterTransformer.generateFilter(getPredicate()))
                            .outputTo(stream)
                            .build();

            reporter.start(getPeriod(), getRealTimeunit());
        }
        catch (Exception e)
        {
            log.error("Failure while enabling console reporter", e);
            return false;
        }
        return true;
    }

}
