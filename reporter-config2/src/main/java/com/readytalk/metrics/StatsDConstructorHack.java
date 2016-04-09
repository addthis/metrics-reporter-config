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

package com.readytalk.metrics;

import com.readytalk.metrics.StatsD;

/**
 * Hack to get around StatsDReporter's constructor design, which requires
 * that a StatsD instance be passed in order to specify a MetricPredicate.
 * This requirement conflicts with all the StatsD constructors being
 * package-private, hence this hack to make the StatsD constructor visible.
 *
 * Once the library has fixed the issue, this hack can be removed. See e.g.:
 * https://github.com/ReadyTalk/metrics-statsd/pull/27
 */
public class StatsDConstructorHack extends StatsD
{
  public StatsDConstructorHack(String host, int port)
  {
    super(host, port);
  }
}
