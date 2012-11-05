/*
 * Copyright (C) 2012 AddThis
 *
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

import com.yammer.metrics.core.MetricPredicate;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public abstract class AbstractReporterConfig
{
    @NotNull
    @Min(1)
    private long period;
    @NotNull
    @Pattern(
        regexp = "^(DAYS|HOURS|MICROSECONDS|MILLISECONDS|MINUTES|NANOSECONDS|SECONDS)$",
        message = "must be a valid java.util.concurrent.TimeUnit"
    )
    private String timeunit;
    @Valid
    private PredicateConfig predicate;


    public long getPeriod()
    {
        return period;
    }

    public void setPeriod(long period)
    {
        this.period = period;
    }

    public String getTimeunit()
    {
        return timeunit;
    }

    public void setTimeunit(String timeunit)
    {
        this.timeunit = timeunit;
    }

    public TimeUnit getRealTimeunit()
    {
        return TimeUnit.valueOf(timeunit);
    }

    public PredicateConfig getPredicate()
    {
        return predicate;
    }

    public void setPredicates(PredicateConfig predicate)
    {
        this.predicate = predicate;
    }

    protected boolean isClassAvailable(String className)
    {
         try
         {
             Class.forName(className);
             return true;
         }
         catch (ClassNotFoundException e)
         {
             return false;
         }
    }

    public MetricPredicate getMetricPredicate()
    {
        if (predicate == null)
        {
            return MetricPredicate.ALL;
        }
        else
        {
            return predicate;
        }
    }

    public abstract boolean enable();
}
