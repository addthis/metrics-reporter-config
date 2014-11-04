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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

abstract class AbstractReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractReporterConfig.class);

    protected static <T> T loadFromFile(String fileName, Class<T> clazz) throws IOException
    {
        Yaml yaml = new Yaml(new Constructor(clazz));
        InputStream input = new FileInputStream(new File(fileName));
        T config = (T) yaml.load(input);
        return config;
    }

    // Based on com.yammer.dropwizard.validation.Validator
    protected static <T> boolean validate(T obj)
    {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Set<ConstraintViolation<T>> violations = factory.getValidator().validate(obj);
        final SortedSet<String> errors = new TreeSet<String>();
        for (ConstraintViolation<T> v : violations)
        {
            errors.add(String.format("%s %s (was %s)",
                    v.getPropertyPath(),
                    v.getMessage(),
                    v.getInvalidValue()));
        }
        if (errors.isEmpty())
        {
            return true;
        }
        else
        {
            log.error("Failed to validate: {}", errors);
            return false;
        }
    }

    public static class ReporterConfigurationException extends RuntimeException
    {
        public ReporterConfigurationException(String msg)
        {
            super(msg);
        }
    }

}
