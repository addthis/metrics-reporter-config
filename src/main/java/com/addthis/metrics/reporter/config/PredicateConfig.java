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

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PredicateConfig implements MetricPredicate
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

    private List<Pattern> cPatterns;

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
        return qualifiedTypeName += "." + mn.getName();
    }


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

}
