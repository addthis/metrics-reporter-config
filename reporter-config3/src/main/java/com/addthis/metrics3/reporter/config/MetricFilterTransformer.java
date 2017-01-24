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
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Timer;
import java.util.List;

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


        /**
         * This will only be invoked if using a fork of the metrics library with support
         * for filtering on a per-measurement basis.
         *
         * Forks supporting this feature:
         * for metrics2: http://github.com/mspiegel/metrics
         * for metrics3 (graphite reporter only): http://github.com/thelastpickle/metrics
         *
         * Otherwise this method is not invoked. The @Override annotation is omitted so
         * that compilation is successful using either the metrics library or the fork of the
         * metrics library.
         */
        public boolean matches(String name, Metric metric, String measurement)
        {
            if ((predicate.getMeter() != null) && (metric instanceof Meter))
            {
                return allowMeasurement(name, measurement, predicate.getMeter(), predicate.getMeterPatterns());
            }
            else if ((predicate.getHistogram() != null) && (metric instanceof Histogram))
            {
                return allowMeasurement(name, measurement, predicate.getHistogram(), predicate.getHistogramPatterns());
            }
            else if ((predicate.getTimer() != null) && (metric instanceof Timer))
            {
                return allowMeasurement(name, measurement, predicate.getTimer(), predicate.getTimerPatterns());
            }
            else
            {
                return true;
            }
        }


        private boolean allowMeasurement(String name, String measurement,
                PredicateConfig.Measurement type, List<PredicateConfig.MeasurementPattern> patterns)
        {
            if (type.getUseQualifiedName())
            {
                return predicate.allowMeasurement(name, measurement, type, patterns);
            }
            else
            {
                return predicate.allowMeasurement(unqualifyMetricName(name), measurement, type, patterns);
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
