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
package com.addthis.metrics3.reporter.config.prometheus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.protobuf.CodedOutputStream;
import io.prometheus.client.Collector;
import io.prometheus.client.Prometheus;

public interface ResponseFormat<O> {

    double FACTOR_TIMER = 1.0d / TimeUnit.SECONDS.toNanos(1L);

    String contentType();

    void writeMetric(MetricsContainer metrics, O writer) throws IOException;

    O createOutput(OutputStream output);

    void finish(O output) throws IOException;

    ResponseFormat TEXT = new TextFormat();
    ResponseFormat PROTOBUF = new ProtobufFormat();

    final class TextFormat implements ResponseFormat<Writer> {
        private static final Logger LOGGER = LoggerFactory.getLogger(TextFormat.class);
        private static final String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

        @Override
        public String contentType() {
            return CONTENT_TYPE_004;
        }

        @Override
        public Writer createOutput(OutputStream output) {
            return new OutputStreamWriter(output);
        }

        @Override
        public void finish(Writer output) throws IOException {
            output.flush();
        }

        @Override
        public void writeMetric(MetricsContainer metrics, Writer writer) throws IOException {
            writer.write("# HELP " + metrics.name + " from dropwizard/codahale\n");
            writer.write("# TYPE " + metrics.name + " " + metrics.typeName + "\n");

            for (MetricInfo metric : metrics.getMetrics()) {
                switch (metrics.type) {
                    case GAUGE:
                    case COUNTER:
                        double value = 0;
                        if (metric.metric instanceof Gauge) {
                            Object obj = ((Gauge) metric.metric).getValue();
                            if (obj instanceof Number) {
                                value = ((Number) obj).doubleValue();
                            } else if (obj instanceof Boolean) {
                                value = ((Boolean) obj) ? 1 : 0;
                            } else {
                                continue;
                            }
                        } else if (metric.metric instanceof Metered) {
                            value = ((Metered) metric.metric).getCount();
                        } else if (metric.metric instanceof Counting) {
                            value = ((Counting) metric.metric).getCount();
                        }
                        writer.write(metrics.name);
                        writer.write(textLabels(metric.labels, true));
                        writer.write(" ");
                        writer.write(Collector.doubleToGoString(value) + "\n");
                        break;
                    case SUMMARY:
                        boolean isTimer = metric.metric instanceof Timer;
                        double factor = isTimer ? FACTOR_TIMER : 1.0d;

                        Snapshot snapshot = ((Sampling) metric.metric).getSnapshot();

                        long sum = 0;
                        for (long i : snapshot.getValues()) {
                            sum += i;
                        }

                        String labels = textLabels(metric.labels, false);
                        try {
                            sampleValue(writer, metrics.name, labels, "0.5", snapshot.getMedian() * factor);
                            sampleValue(writer, metrics.name, labels, "0.75", snapshot.get75thPercentile() * factor);
                            sampleValue(writer, metrics.name, labels, "0.95", snapshot.get95thPercentile() * factor);
                            sampleValue(writer, metrics.name, labels, "0.98", snapshot.get98thPercentile() * factor);
                            sampleValue(writer, metrics.name, labels, "0.99", snapshot.get99thPercentile() * factor);
                            sampleValue(writer, metrics.name, labels, "0.999", snapshot.get999thPercentile() * factor);
                            sampleValue(writer, metrics.name + "_count", labels, null, ((Counting) metric.metric).getCount());
                            sampleValue(writer, metrics.name + "_sum", labels, null, sum * factor);
                        }
                        catch (Exception e) {
                            LOGGER.warn("Failed to build metric values for {} ({}) due to {}", metrics.name, metric.sourceName, e.toString());
                        }
                        break;
                }
            }
        }

        private String textLabels(String[][] labels, boolean withBrackets) {
            if (labels.length == 0)
                return "";
            StringBuilder sb = new StringBuilder();
            if (withBrackets)
                sb.append('{');
            for (String[] label : labels) {
                sb.append(label[0]).append('=').append(escapeLabelValue(label[1])).append(',');
            }
            if (withBrackets)
                sb.append('}');
            return sb.toString();
        }

        static String escapeLabelValue(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }

        private void sampleValue(Writer writer, String name, String labels, String quant, double v) throws IOException {
            writer.write(name);
            if (quant != null || labels != null)
                writer.write('{');
            if (labels != null) {
                writer.write(labels);
            }
            if (quant != null) {
                writer.write("quantile=\"");
                writer.write(quant);
                writer.write("\"");
            }
            if (quant != null || labels != null)
                writer.write('}');
            writer.write(' ');
            writer.write(Collector.doubleToGoString(v));
            writer.write('\n');
        }
    }

    final class ProtobufFormat implements ResponseFormat<CodedOutputStream> {
        private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufFormat.class);
        private static final String CONTENT_TYPE_004 = "application/vnd.google.protobuf;proto=io.prometheus.client.MetricFamily;encoding=delimited";

        @Override
        public String contentType() {
            return CONTENT_TYPE_004;
        }

        @Override
        public void finish(CodedOutputStream output) throws IOException {
            output.flush();
        }

        @Override
        public CodedOutputStream createOutput(OutputStream output) {
            return CodedOutputStream.newInstance(output);
        }

        @Override
        public void writeMetric(MetricsContainer metrics, CodedOutputStream writer) throws IOException {
            Prometheus.MetricFamily.Builder builder = Prometheus.MetricFamily.newBuilder().
                    setHelp("from dropwizard/codahale " + metrics.name).
                    setName(metrics.name).
                    setType(metrics.type);

            for (MetricInfo metric : metrics.getMetrics()) {
                switch (metrics.type) {
                    case GAUGE:
                        Object obj = ((Gauge) metric.metric).getValue();
                        double value;
                        if (obj instanceof Number) {
                            value = ((Number) obj).doubleValue();
                        } else if (obj instanceof Boolean) {
                            value = ((Boolean) obj) ? 1 : 0;
                        } else {
                            return;
                        }
                        Prometheus.Metric.Builder metricBuilder = builder.addMetricBuilder();
                        addLabels(metricBuilder, metric);
                        metricBuilder.setGauge(Prometheus.Gauge.newBuilder().setValue(value));
                        break;
                    case COUNTER:
                        long v = ((Counting) metric.metric).getCount();
                        metricBuilder = builder.addMetricBuilder();
                        addLabels(metricBuilder, metric);
                        metricBuilder.setCounter(Prometheus.Counter.newBuilder().setValue(v));
                        break;
                    case SUMMARY:
                        boolean isTimer = metric.metric instanceof Timer;
                        double factor = isTimer ? FACTOR_TIMER : 1.0d;
                        fromSnapshotAndCount(builder, metrics, metric,
                                ((Sampling) metric.metric).getSnapshot(),
                                ((Counting) metric.metric).getCount(), factor);
                        break;
                }
// TODO somehow possible to add mean, one-minute, five-minute, fifteen-minute rates along WITH the histogram/meter ?
//                if (metric.metric instanceof Metered) {
//                    Metered metered = (Metered) metric.metric;
//                    metered.getMeanRate();
//                    metered.getOneMinuteRate();
//                    metered.getFiveMinuteRate();
//                    metered.getFifteenMinuteRate();
//                }
            }

            if (builder.getMetricCount() > 0) {
                writer.writeMessageNoTag(builder.build());
            }
        }

        private void addLabels(Prometheus.Metric.Builder metricBuilder, MetricInfo metric) {
            for (String[] label : metric.labels) {
                metricBuilder.addLabelBuilder()
                        .setName(label[0])
                        .setValue(label[1]);
            }
        }

        private void fromSnapshotAndCount(Prometheus.MetricFamily.Builder builder, MetricsContainer metrics, MetricInfo metric, Snapshot snapshot, long count, double factor) {
            long sum = 0;
            for (long i : snapshot.getValues()) {
                sum += i;
            }

            try {
                Prometheus.Summary.Builder summaryBuilder = Prometheus.Summary.newBuilder();
                summaryBuilder.addQuantileBuilder().setQuantile(.5d).setValue(snapshot.getMedian() * factor);
                summaryBuilder.addQuantileBuilder().setQuantile(.75d).setValue(snapshot.get75thPercentile() * factor);
                summaryBuilder.addQuantileBuilder().setQuantile(.95d).setValue(snapshot.get95thPercentile() * factor);
                summaryBuilder.addQuantileBuilder().setQuantile(.98d).setValue(snapshot.get98thPercentile() * factor);
                summaryBuilder.addQuantileBuilder().setQuantile(.99d).setValue(snapshot.get99thPercentile() * factor);
                summaryBuilder.addQuantileBuilder().setQuantile(.999d).setValue(snapshot.get999thPercentile() * factor);
                summaryBuilder.setSampleCount(count);
                summaryBuilder.setSampleSum(sum * factor);
                Prometheus.Metric.Builder metricBuilder = builder.addMetricBuilder();
                addLabels(metricBuilder, metric);
                metricBuilder.setSummary(summaryBuilder);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to build metric values for {} ({}) due to {}", metrics.name, metric.sourceName, e.toString());
            }
        }
    }
}
