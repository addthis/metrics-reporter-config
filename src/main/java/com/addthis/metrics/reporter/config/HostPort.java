package com.addthis.metrics.reporter.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class HostPort
{
    @NotNull
    private String host;
    @NotNull
    @Min(0)
    @Max(65535)
    private int port;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}


