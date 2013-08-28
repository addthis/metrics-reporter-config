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

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PredicateConfig implements MetricPredicate
{
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

    private List<Pattern> cPatterns;

    public PredicateConfig() {}

    public PredicateConfig(String color, List<String> patterns)
    {
        setColor(color);
        setPatterns(patterns);
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


    @Override
    public boolean matches(MetricName name, Metric metric)
    {
        return allowString(name.getName());
    }

}
