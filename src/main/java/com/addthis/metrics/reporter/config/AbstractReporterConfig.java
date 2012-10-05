package com.addthis.metrics.reporter.config;

import java.lang.Class;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public abstract class AbstractReporterConfig {
    @NotNull
    @Min(1)
    protected long period;
    @NotNull
    protected String timeunit;
    
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


    protected boolean isClassAvailable(String className)
    {
         try {
             Class.forName(className);
             return true;
         } catch(ClassNotFoundException e) {
             return false;
         }
    }

    public abstract void enable();
}