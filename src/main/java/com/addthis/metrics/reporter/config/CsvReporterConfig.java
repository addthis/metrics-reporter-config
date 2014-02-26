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
import com.codahale.metrics.CsvReporter;

import java.io.File;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CsvReporterConfig extends AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(CsvReporterConfig.class);

    @NotNull
    private String outdir;

    public String getOutdir()
    {
        return outdir;
    }

    public void setOutdir(String outdir)
    {
        this.outdir = outdir;
    }

    @Override
    public boolean enable(MetricRegistry registry)
    {
        log.info("Enabling CsvReporter to {}", outdir);
        try
        {
            File foutDir = new File(outdir);
            boolean success = foutDir.mkdirs();
            if (!success)
            {
                log.error("Failed to create directory {} for CsvReporter", outdir);
                return false;
            }
            // static enable() methods omit the option of specifying a
            // predicate.  Calling constructor and starting manually
            // instead
            final CsvReporter reporter = CsvReporter.forRegistry(registry)
                    .convertRatesTo(getRealRateunit())
                    .convertDurationsTo(getRealDurationunit())
                    .filter(getMetricPredicate())
                    .build(foutDir);
            reporter.start(getPeriod(), getRealTimeunit());
        }
        catch (Exception e)
        {
            log.error("Failure while Enabling CsvReporter", e);
            return false;
        }
        return true;
    }

}
