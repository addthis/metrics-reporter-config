package com.addthis.metrics.reporter.config;

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
            log.trace("Checking Metric name: {} {}", new Object[] {name, PredicateConfig.unqualifyMetricName(name)});
            if (predicate.getUseQualifiedName())
            {
                return predicate.allowString(name);
            }
            else
            {
                return predicate.allowString(PredicateConfig.unqualifyMetricName(name));
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
