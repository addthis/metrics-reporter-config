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

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPrometheusReporterConfig extends AbstractHostPortReporterConfig {
    public enum Type {
        pushgateway, servlet;
    }

    protected String name = "prometheus";

    @NotNull
    protected Type type;

    @NotNull
    protected String job;

    protected Map<String, String> labels = new HashMap<>();
    protected Map<String, String> resolvedLabels = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return this.job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            this.resolvedLabels.put(entry.getKey(), resolvePrefix(entry.getValue()));
        }
    }

    public Map<String, String> getResolvedLabels() {
        return resolvedLabels;
    }
}
