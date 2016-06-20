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

import com.addthis.metrics.reporter.config.PredicateConfig;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricFilterTransformer
{

    private static final Logger log = LoggerFactory.getLogger(MetricFilterTransformer.class);

    private static class PredicateConfigFilter implements MetricFilter
    {
        final PredicateConfig predicate;

        PredicateConfigFilter(PredicateConfig predicate)
        {
            this.predicate = predicate;
        }

        @Override
        public boolean matches(String name, Metric metric)
        {
            log.trace("Checking Metric name: {} {}", new Object[] {name, unqualifyMetricName(name)});
            if (predicate.getUseQualifiedName())
            {
                return predicate.allowString(name);
            }
            else
            {
                return predicate.allowString(unqualifyMetricName(name));
            }
        }

        private static String unqualifyMetricName(String name)
        {
            int location = name.lastIndexOf('.');
            if (location < 0)
            {
                return name;
            }
            else
            {
                return name.substring(location + 1);
            }
        }

    }

    public static MetricFilter generateFilter(PredicateConfig predicate)
    {
        if (predicate == null)
        {
            return MetricFilter.ALL;
        }
        else
        {
            return new PredicateConfigFilter(predicate);
        }
    }

}
