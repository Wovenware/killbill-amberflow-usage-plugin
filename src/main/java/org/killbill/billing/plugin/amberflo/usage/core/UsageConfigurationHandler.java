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

package org.killbill.billing.plugin.amberflo.usage.core;

import java.util.Properties;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.plugin.api.notification.PluginTenantConfigurableConfigurationHandler;

/**
 * When per-tenant config changes are made, the plugin automatically gets notified (and prints a log
 * trace)
 */
public class UsageConfigurationHandler
    extends PluginTenantConfigurableConfigurationHandler<UsageConfigProperties> {

  private final String region;

  public UsageConfigurationHandler(
      final String region, final String pluginName, final OSGIKillbillAPI osgiKillbillAPI) {
    super(pluginName, osgiKillbillAPI);
    this.region = region;
  }

  @Override
  protected UsageConfigProperties createConfigurable(final Properties properties) {
    return new UsageConfigProperties(properties, region);
  }
}
