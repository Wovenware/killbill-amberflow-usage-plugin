/*
 * Copyright 2023 Wovenware, Inc
 *
 * Wovenware licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.usage.core;

import java.util.Map;
import javax.annotation.Nullable;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.tenant.api.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageHealthcheck implements Healthcheck {
  private static final Logger logger = LoggerFactory.getLogger(UsageHealthcheck.class);

  @Override
  public HealthStatus getHealthStatus(
      @Nullable final Tenant tenant, @Nullable final Map properties) {
    if (tenant == null) {
      // The plugin is running
      return HealthStatus.healthy("Usage OK");
    } else {
      // Specifying the tenant lets you also validate the tenant configuration

      return pingGatewayService();
    }
  }

  private HealthStatus pingGatewayService() {

    try {

      return HealthStatus.healthy("Usage OK");
    } catch (final Exception e) {
      logger.warn("Usage error", e);
      return HealthStatus.unHealthy("Usage error: " + e.getMessage());
    }
  }
}
