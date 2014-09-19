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

import com.codahale.metrics.MetricFilter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.yammer.metrics.core.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PredicateConfig implements MetricPredicate, MetricFilter
{
    private static final Logger log = LoggerFactory.getLogger(PredicateConfig.class);


    // white ||, black &&
    @NotNull
    @javax.validation.constraints.Pattern(
        regexp = "^(white|black)$",
        message = "must one of: white, black"
    )
    private String color;
    @NotNull
    @Size(min=1)
    private List<String> patterns;
    private boolean useQualifiedName;
    private Measurement meter;
    private Measurement histogram;
    private Measurement timer;

    public static class Measurement
    {
        @NotNull
        @javax.validation.constraints.Pattern(
                regexp = "^(white|black)$",
                message = "must one of: white, black"
        )
        private String color;
        @NotNull
        @Size(min=1)
        private List<MeasurementSpecification> patterns;
        private boolean useQualifiedName;

        public Measurement() {}

        public Measurement(String color, boolean useQualifiedName)
        {
            this.color = color;
            this.useQualifiedName = useQualifiedName;
        }

        public String getColor() { return color; }
        public boolean getUseQualifiedName() { return useQualifiedName; }
        public List<MeasurementSpecification> getPatterns() { return patterns; }
        public void setColor(String color) { this.color = color; }
        public void setUseQualifiedName(boolean value) { this.useQualifiedName = value; }
        public void setPatterns(List<MeasurementSpecification> patterns) { this.patterns = patterns; }
    }

    public static class MeasurementSpecification
    {
        @NotNull
        private String metric;
        @NotNull
        private String measure;

        public MeasurementSpecification() {}
        public String getMetric() { return metric; }
        public String getMeasure() { return measure; }
        public void setMetric(String metric) { this.metric = metric; }
        public void setMeasure(String measure) { this.measure = measure; }
    }

    public static class MeasurementPattern
    {
        @NotNull
        private Pattern metric;
        @NotNull
        private Pattern measure;


        public MeasurementPattern(String metric, String measure)
        {
            this.metric = Pattern.compile(metric);
            this.measure = Pattern.compile(measure);
        }
    }


    private List<Pattern> cPatterns;
    private List<MeasurementPattern> meterPatterns;
    private List<MeasurementPattern> histogramPatterns;
    private List<MeasurementPattern> timerPatterns;

    public PredicateConfig() {}

    public PredicateConfig(String color, List<String> patterns)
    {
        this(color, patterns, false);
    }

    public PredicateConfig(String color, List<String> patterns, boolean useQualifiedName)
    {
        setColor(color);
        setPatterns(patterns);
        setUseQualifiedName(useQualifiedName);
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public List<String> getPatterns()
    {
        return patterns;
    }

    public void setPatterns(List<String> patterns)
    {
        this.patterns = patterns;
        cPatterns = new ArrayList<Pattern>();
        for (String s : patterns)
        {
            cPatterns.add(Pattern.compile(s));
        }
    }

    public boolean getUseQualifiedName()
    {
        return useQualifiedName;
    }

    public void setUseQualifiedName(boolean useQualifiedName)
    {
        this.useQualifiedName = useQualifiedName;
    }

    public Measurement getMeter()
    {
        return meter;
    }

    public Measurement getHistogram()
    {
        return histogram;
    }

    public Measurement getTimer()
    {
        return timer;
    }

    public void setMeter(Measurement meter)
    {
        this.meter = meter;
        this.meterPatterns = createMeasurementPatterns(meter);
    }

    public void setHistogram(Measurement histogram)
    {
        this.histogram = histogram;
        this.histogramPatterns = createMeasurementPatterns(histogram);
    }

    public void setTimer(Measurement timer)
    {
        this.timer = timer;
        this.timerPatterns = createMeasurementPatterns(timer);
    }

    private List<MeasurementPattern> createMeasurementPatterns(Measurement measurement)
    {
        List<MeasurementPattern> result = null;
        if (measurement != null)
        {
            result = new ArrayList<MeasurementPattern>();
            for (MeasurementSpecification s : measurement.patterns)
            {
                result.add(new MeasurementPattern(s.metric, s.measure));
            }
        }
        return result;
    }

    public boolean allowString(String name)
    {

        if (color.equals("black"))
        {
            for (Pattern pat : cPatterns)
            {
                if (pat.matcher(name).matches())
                {
                    return false;
                }
            }
            return true;
        }

        if (color.equals("white"))
        {
            for (Pattern pat : cPatterns)
            {
                if (pat.matcher(name).matches())
                {
                    return true;
                }
            }
        }
        return false; // trusting validator
    }

    // Qualify (ie include class name and whatnot -- the metric name.
    // MetricName.getName() is just the last part of that.  Joining on
    // "." and based on code from MetricsRegistry.groupedMetrics()
    public String qualifyMetricName(MetricName mn)
    {
        String qualifiedTypeName = mn.getGroup() + "." + mn.getType();
        if (mn.hasScope())
        {
            qualifiedTypeName += "." + mn.getScope();
        }
        qualifiedTypeName += "." + mn.getName();
        return qualifiedTypeName;
    }

    public String unqualifyMetricName(String name) {
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

    /**
     * Metrics 2.x support
     */
    @Override
    public boolean matches(MetricName name, Metric metric)
    {
        log.trace("Checking Metric name: {} {}", new Object[] {name.getName(), qualifyMetricName(name)});
        if (useQualifiedName)
        {
            return allowString(qualifyMetricName(name));
        }
        else
        {
            return allowString(name.getName());
        }
    }

    /**
     * Metrics 3.x support
     */
    @Override
    public boolean matches(String name, com.codahale.metrics.Metric metric)
    {
        log.trace("Checking Metric name: {} {}", new Object[] {name, unqualifyMetricName(name)});
        if (useQualifiedName)
        {
            return allowString(name);
        }
        else
        {
            return allowString(unqualifyMetricName(name));
        }
    }

    public static boolean allowMeasurement(String name, String measurement,
                                           Measurement type, List<MeasurementPattern> patterns)
    {
        if (type.color.equals("black"))
        {
            for (int i = 0; i < patterns.size(); i++)
            {
                Pattern metricPattern = patterns.get(i).metric;
                Pattern measurePattern = patterns.get(i).measure;
                if (metricPattern.matcher(name).matches() &&
                        measurePattern.matcher(measurement).matches())
                {
                    return false;
                }
            }
            return true;
        }

        if (type.color.equals("white"))
        {
            for (int i = 0; i < patterns.size(); i++)
            {
                Pattern metricPattern = patterns.get(i).metric;
                Pattern measurePattern = patterns.get(i).measure;
                if (metricPattern.matcher(name).matches() &&
                        measurePattern.matcher(measurement).matches())
                {
                    return true;
                }
            }
        }
        return false; // trusting validator
    }

    public boolean allowMeasurement(MetricName name, String measurement,
                                    Measurement type, List<MeasurementPattern> patterns)
    {
        if (type.useQualifiedName)
        {
            return allowMeasurement(qualifyMetricName(name), measurement, type, patterns);
        }
        else
        {
            return allowMeasurement(name.getName(), measurement, type, patterns);
        }
    }

    /**
     * This will only be invoked if using a fork of the metrics library with support
     * for filtering on a per-measurement basis - http://github.com/mspiegel/metrics.
     * Otherwise this method is not invoked. The @Override annotation is omitted so
     * that compilation is successful using either the metrics library or the fork of the
     * metrics library.
     */
    public boolean matches(MetricName name, Metric metric, String measurement)
    {
        if ((meter != null) && (metric instanceof Meter))
        {
            return allowMeasurement(name, measurement, meter, meterPatterns);
        }
        else if ((histogram != null) && (metric instanceof Histogram))
        {
            return allowMeasurement(name, measurement, histogram, histogramPatterns);
        }
        else if ((timer != null) && (metric instanceof Timer))
        {
            return allowMeasurement(name, measurement, timer, timerPatterns);
        }
        else
        {
            return true;
        }
    }

}
