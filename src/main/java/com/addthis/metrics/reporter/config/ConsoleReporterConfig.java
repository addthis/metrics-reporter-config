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

import com.codahale.metrics.MetricRegistry;
import com.yammer.metrics.Metrics;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsoleReporterConfig extends AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(ConsoleReporterConfig.class);

    private String outfile = null;

    public String getOutfile()
    {
        return outfile;
    }

    public void setOutfile(String outfile)
    {
        this.outfile = outfile;
    }

    private PrintStream createPrintStream() throws FileNotFoundException
    {
        if (outfile != null)
        {
            log.info("console reporting will be redirected to {} instead of stdout", outfile);
            return new PrintStream(outfile);
        }
        else
        {
            return System.out;
        }
    }

    @Override
    public boolean enable(MetricRegistry registry)
    {
        try
        {
            PrintStream stream = createPrintStream();

            final com.codahale.metrics.ConsoleReporter reporter =
                    com.codahale.metrics.ConsoleReporter.forRegistry(registry)
                .convertRatesTo(getRealRateunit())
                .convertDurationsTo(getRealDurationunit())
                .filter(getMetricFilter())
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

    @Override
    public boolean enable()
    {
        try
        {
            PrintStream stream = createPrintStream();

            // static enable() methods omit the option of specifying a
            // predicate.  Calling constructor and starting manually
            // instead
            final com.yammer.metrics.reporting.ConsoleReporter reporter =
                    new com.yammer.metrics.reporting.ConsoleReporter(
                            Metrics.defaultRegistry(),
                            stream,
                            getMetricPredicate());

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
