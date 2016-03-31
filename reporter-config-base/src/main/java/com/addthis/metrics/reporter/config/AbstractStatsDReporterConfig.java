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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractStatsDReporterConfig extends AbstractHostPortReporterConfig
{
    private static final Logger log = LoggerFactory.getLogger(AbstractStatsDReporterConfig.class);

    @Override
    public List<HostPort> getFullHostList()
    {
        return getHostListAndStringList();
    }

    protected boolean setup(String className)
    {
        if (!isClassAvailable(className))
        {
            log.error("Tried to enable StatsD Reporter, but class {} was not found", className);
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty())
        {
            log.error("No hosts specified, cannot enable StatsD Reporter");
            return false;
        }
        return true;
    }
}
