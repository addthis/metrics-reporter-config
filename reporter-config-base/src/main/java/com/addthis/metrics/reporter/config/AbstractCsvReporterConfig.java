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

import java.io.File;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractCsvReporterConfig extends AbstractMetricReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractCsvReporterConfig.class);

    @NotNull
    protected String outdir;

    public String getOutdir()
    {
        return outdir;
    }

    public void setOutdir(String outdir)
    {
        this.outdir = outdir;
    }

    protected File createFile()
    {
        File foutDir = new File(outdir);
        boolean success = foutDir.mkdirs();
        if (!success)
        {
            log.error("Failed to create directory {} for CsvReporter", outdir);
            return null;
        }
        return foutDir;
    }

}
