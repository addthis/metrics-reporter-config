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

import java.net.InetAddress;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

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
