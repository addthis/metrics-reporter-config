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

import java.util.List;

import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricPredicateTransformer
{
    private static final Logger log = LoggerFactory.getLogger(MetricPredicateTransformer.class);

    private MetricPredicateTransformer () {}

    static class PredicateConfigMetricPredicate implements MetricPredicate
    {
        final PredicateConfig predicate;

        PredicateConfigMetricPredicate(PredicateConfig predicate)
        {
            this.predicate = predicate;
        }

        // Qualify (ie include class name and whatnot -- the metric name.
        // MetricName.getName() is just the last part of that.  Joining on
        // "." and based on code from MetricsRegistry.groupedMetrics()
        public static String qualifyMetricName(MetricName mn)
        {
            String qualifiedTypeName = mn.getGroup() + "." + mn.getType();
            if (mn.hasScope())
            {
                qualifiedTypeName += "." + mn.getScope();
            }
            qualifiedTypeName += "." + mn.getName();
            return qualifiedTypeName;
        }

        public boolean allowMeasurement(MetricName name, String measurement,
                PredicateConfig.Measurement type, List<PredicateConfig.MeasurementPattern> patterns)
        {
            if (type.getUseQualifiedName())
            {
                return predicate.allowMeasurement(qualifyMetricName(name), measurement, type, patterns);
            }
            else
            {
                return predicate.allowMeasurement(name.getName(), measurement, type, patterns);
            }
        }

        @Override
        public boolean matches(MetricName name, Metric metric)
        {
            log.trace("Checking Metric name: {} {}", new Object[] {name.getName(), qualifyMetricName(name)});
            if (predicate.getUseQualifiedName())
            {
                return predicate.allowString(qualifyMetricName(name));
            }
            else
            {
                return predicate.allowString(name.getName());
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
        public boolean matches(MetricName name, Metric metric, String measurement)
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
    }

    public static MetricPredicate generatePredicate(PredicateConfig predicate)
    {
        if (predicate == null)
        {
            return MetricPredicate.ALL;
        }
        else
        {
            return new PredicateConfigMetricPredicate(predicate);
        }
    }

}
