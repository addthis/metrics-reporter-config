package com.addthis.metrics.reporter.config;

import java.net.InetAddress;

import com.addthis.metrics.reporter.config.AbstractGraphiteReporterConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphiteReporterConfigTest {
    @Test
    public void prefixValueSubstitution() {
        InetAddress addressMock = mock(InetAddress.class);
        when(addressMock.getHostName()).thenReturn(
                "1host\u10B0-na_me3.domain1.com");
        when(addressMock.getHostAddress()).thenReturn("1.2.3.4");
        when(addressMock.getCanonicalHostName()).thenReturn(
                "1host\u10B0-na_me3.domain1.com");

        AbstractGraphiteReporterConfig reporter = new AbstractGraphiteReporterConfig(
                addressMock);
        reporter.setPrefix("static.${host.name}.${host.address}.${host.fqdn}.${host.name.short}.${undef}");
        assertEquals(
                "static.1host_-na_me3_domain1_com.1_2_3_4.1host_-na_me3_domain1_com.1host_-na_me3.${undef}",
                reporter.getResolvedPrefix());
    }

    @Test
    public void nullPrefix() {
        AbstractGraphiteReporterConfig reporter = new AbstractGraphiteReporterConfig();
        reporter.setPrefix(null);
        assertNull(reporter.getResolvedPrefix());
    }

    @Test
    public void emptyPrefix() {
        AbstractGraphiteReporterConfig reporter = new AbstractGraphiteReporterConfig();
        reporter.setPrefix("");
        assertEquals("", reporter.getResolvedPrefix());
    }

    @Test
    public void plainPrefix() {
        AbstractGraphiteReporterConfig reporter = new AbstractGraphiteReporterConfig();
        reporter.setPrefix("testprefix");
        assertEquals("testprefix", reporter.getResolvedPrefix());
    }
}
