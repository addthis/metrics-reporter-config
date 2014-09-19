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

package com.addthis.metrics.reporter.config.sample;

import java.io.IOException;

import com.addthis.metrics.reporter.config.ReporterConfig;

import com.codahale.metrics.MetricRegistry;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


// TODO: Make this an integration test
public class NameTest
{
    private static final Logger log = LoggerFactory.getLogger(NameTest.class);

    private static final Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));

    private void recursiveDelete(Path directory) throws IOException
    {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    // Without mocking up a full repoter & registry it's surprisingly
    // difficult to get the MetricName of a metric, or trace the
    // application of predicates.  This is a hacky test for visual
    // inspection.
    @Test
    public void csvNamePrintingMetrics2() throws Exception
    {
        log.debug("name test metrics 2.x");
        com.yammer.metrics.core.Counter counter = com.yammer.metrics.Metrics.newCounter(getClass(), "mycounter");
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/csv-predicate.yaml");
        recursiveDelete(Paths.get(config.getCsv().get(0).getOutdir()));
        config.enableAll2();
        counter.inc();
        Thread.sleep(10000);
    }

    @Test
    public void csvNamePrintingMetrics3() throws Exception
    {
        log.debug("name test metrics 3.x");
        final MetricRegistry metrics = new MetricRegistry();
        com.codahale.metrics.Counter counter = metrics.counter("mycounter");
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/csv-predicate.yaml");
        recursiveDelete(Paths.get(config.getCsv().get(0).getOutdir()));
        config.enableAll3(metrics);
        counter.inc();
        Thread.sleep(10000);
    }

}
