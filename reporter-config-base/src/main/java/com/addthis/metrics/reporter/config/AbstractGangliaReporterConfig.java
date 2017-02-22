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


import javax.validation.constraints.NotNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractGangliaReporterConfig extends AbstractHostPortReporterConfig {
    private static final Logger log = LoggerFactory.getLogger(AbstractGangliaReporterConfig.class);

    @NotNull
    protected String groupPrefix = "";
    @NotNull
    protected boolean compressPackageNames = false;
    protected String gmondConf;
    protected String spoofName;

    public boolean getCompressPackageNames() {
        return compressPackageNames;
    }

    public void setCompressPackageNames(boolean compressPackageNames) {
        this.compressPackageNames = compressPackageNames;
    }

    public String getGmondConf() {
        return gmondConf;
    }

    public void setGmondConf(String gmondConf) {
        this.gmondConf = gmondConf;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    public String getSpoofName() {
        return spoofName;
    }

    public void setSpoofName(String spoofName) {
        this.spoofName = spoofName;
    }

    @Override
    public List<HostPort> getFullHostList() {
        if (gmondConf != null) {
            GmondConfigParser gcp = new GmondConfigParser();
            List<HostPort> confHosts = gcp.getGmondSendChannels(gmondConf);
            if (confHosts == null || confHosts.isEmpty()) {
                log.warn("No send channels found after reading {}", gmondConf);
            }
            return confHosts;
        } else {
            return getHostListAndStringList();
        }
    }

    protected boolean setup(String className) {
        if (!isClassAvailable(className)) {
            log.error("Tried to enable GangliaReporter, but class {} was not found", className);
            return false;
        }
        List<HostPort> hosts = getFullHostList();
        if (hosts == null || hosts.isEmpty()) {
            log.error("No hosts specified, cannot enable GangliaReporter");
            return false;
        }
        return true;
    }
}
