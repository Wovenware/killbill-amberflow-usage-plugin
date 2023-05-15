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

import java.util.Hashtable;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.billing.plugin.amberflo.usage.api.UsagePluginApiImpl;
import org.killbill.billing.plugin.api.notification.PluginConfigurationEventHandler;
import org.killbill.billing.plugin.core.config.PluginEnvironmentConfig;
import org.killbill.billing.usage.plugin.api.UsagePluginApi;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageActivator extends KillbillActivatorBase {

  private static final Logger logger = LoggerFactory.getLogger(UsageActivator.class);

  public static final String PLUGIN_NAME = "amberflo-usage-plugin";

  private UsageConfigurationHandler usageConfigurationHandler;

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);

    logger.info(" starting plugin {}", PLUGIN_NAME);

    final String region = PluginEnvironmentConfig.getRegion(configProperties.getProperties());

    // Register an event listener for plugin configuration (optional)
    logger.info("Registering an event listener for plugin configuration");
    usageConfigurationHandler = new UsageConfigurationHandler(region, PLUGIN_NAME, killbillAPI);
    final UsageConfigProperties globalConfiguration =
        usageConfigurationHandler.createConfigurable(configProperties.getProperties());
    usageConfigurationHandler.setDefaultConfigurable(globalConfiguration);

    logger.info("Registering an APIs");
    final UsagePluginApi paymentPluginApi =
        new UsagePluginApiImpl(usageConfigurationHandler, killbillAPI);
    registerUsagePluginApi(context, paymentPluginApi);

    // Expose a healthcheck (optional), so other plugins can check on the plugin status
    logger.info("Registering healthcheck");
    final Healthcheck healthcheck = new UsageHealthcheck();
    registerHealthcheck(context, healthcheck);

    registerHandlers();
  }

  private void registerHandlers() {
    final PluginConfigurationEventHandler configHandler =
        new PluginConfigurationEventHandler(usageConfigurationHandler);
    dispatcher.registerEventHandlers(configHandler);
  }

  private void registerUsagePluginApi(final BundleContext context, final UsagePluginApi api) {
    final Hashtable<String, String> props = new Hashtable<>();
    props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
    registrar.registerService(context, UsagePluginApi.class, api, props);
  }

  private void registerHealthcheck(final BundleContext context, final Healthcheck healthcheck) {
    final Hashtable<String, String> props = new Hashtable<>();
    props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
    registrar.registerService(context, Healthcheck.class, healthcheck, props);
  }
}
