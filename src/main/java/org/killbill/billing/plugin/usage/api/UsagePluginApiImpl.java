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

package org.killbill.billing.plugin.usage.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionApiException;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.plugin.usage.client.UsageClientImpl;
import org.killbill.billing.plugin.usage.client.UsageProcessorImpl;
import org.killbill.billing.plugin.usage.core.UsageConfigurationHandler;
import org.killbill.billing.usage.api.RawUsageRecord;
import org.killbill.billing.usage.plugin.api.UsageContext;
import org.killbill.billing.usage.plugin.api.UsagePluginApi;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.clock.Clock;

public class UsagePluginApiImpl implements UsagePluginApi {

  private final UsageConfigurationHandler usageConfigurationHandler;
  private final OSGIKillbillAPI killbillAPI;

  private final OSGIConfigPropertiesService configProperties;

  private final Clock clock;

  public UsagePluginApiImpl(
      final UsageConfigurationHandler usageConfigurationHandler,
      final OSGIKillbillAPI killbillAPI,
      final OSGIConfigPropertiesService configProperties,
      final Clock clock) {
    this.usageConfigurationHandler = usageConfigurationHandler;
    this.killbillAPI = killbillAPI;
    this.configProperties = configProperties;
    this.clock = clock;
  }

  @Override
  public List<RawUsageRecord> getUsageForAccount(
      DateTime startDate,
      DateTime endDate,
      UsageContext context,
      Iterable<PluginProperty> properties) {

    UsageClientImpl client =
        new UsageClientImpl(usageConfigurationHandler.getConfigurable(context.getTenantId()));
    UsageProcessorImpl processor = new UsageProcessorImpl(client);
    return null;
  }

  @Override
  public List<RawUsageRecord> getUsageForSubscription(
      UUID subscriptionId,
      DateTime startDate,
      DateTime endDate,
      UsageContext context,
      Iterable<PluginProperty> properties) {
    UsageClientImpl client =
        new UsageClientImpl(usageConfigurationHandler.getConfigurable(context.getTenantId()));
    UsageProcessorImpl processor = new UsageProcessorImpl(client);
    return null;
  }

  private String getExternalKeyFromAccount(UUID accountId, TenantContext context) {
    try {
      return killbillAPI.getAccountUserApi().getAccountById(accountId, context).getExternalKey();
    } catch (AccountApiException e) {
      // add logger
    }
    return null;
  }

  private Optional<Subscription> getSubscriptionData(
      UUID accountId, TenantContext context, UUID subscriptionId) {
    try {

      List<SubscriptionBundle> subscriptionBundles =
          killbillAPI.getSubscriptionApi().getSubscriptionBundlesForAccountId(accountId, context);
      List<Subscription> subscriptions =
          subscriptionBundles.stream()
              .flatMap(p -> p.getSubscriptions().stream())
              .collect(Collectors.toList());
      return subscriptions.stream()
          .filter(s -> s.getId().toString().equalsIgnoreCase(subscriptionId.toString()))
          .findFirst();
    } catch (SubscriptionApiException e) {
      // add logger
    }
    return null;
  }
}
