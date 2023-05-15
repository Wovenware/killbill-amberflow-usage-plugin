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

package org.killbill.billing.plugin.amberflo.usage.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionApiException;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.plugin.amberflo.usage.api.model.RawUsageRecordImpl;
import org.killbill.billing.plugin.amberflo.usage.client.AmberfloClientImpl;
import org.killbill.billing.plugin.amberflo.usage.core.UsageConfigurationHandler;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.usage.api.RawUsageRecord;
import org.killbill.billing.usage.plugin.api.UsageContext;
import org.killbill.billing.usage.plugin.api.UsagePluginApi;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.customfield.CustomField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsagePluginApiImpl implements UsagePluginApi {

  private final UsageConfigurationHandler usageConfigurationHandler;
  private final OSGIKillbillAPI killbillAPI;
  private static final Logger logger = LoggerFactory.getLogger(UsagePluginApiImpl.class);

  public UsagePluginApiImpl(
      final UsageConfigurationHandler usageConfigurationHandler,
      final OSGIKillbillAPI killbillAPI) {
    this.usageConfigurationHandler = usageConfigurationHandler;
    this.killbillAPI = killbillAPI;
  }

  @Override
  public List<RawUsageRecord> getUsageForAccount(
      DateTime startDate,
      DateTime endDate,
      UsageContext context,
      Iterable<PluginProperty> properties) {
    logger.info(
        "[getUsageForAccount] for account {} from {} to {} ",
        context.getAccountId(),
        startDate,
        endDate);

    // When making a call, the dates cannot be null and the end date cannot be earlier than the
    // start date. If any of these criteria is not met, then it returns an empty collection.
    if (startDate == null || endDate == null) {
      logger.error("Dates cannot be null!");
      return Collections.emptyList();
    } else if (startDate.compareTo(endDate) > 0) {
      logger.error("End date cannot be sooner than start date!");
      return Collections.emptyList();
    }

    startDate = sanitizeDateTime(startDate, true);
    endDate = sanitizeDateTime(endDate, false);

    String accountExternalKey = this.getExternalKeyFromAccount(context.getAccountId(), context);
    AmberfloClientImpl client =
        new AmberfloClientImpl(
            usageConfigurationHandler.getConfigurable(context.getTenantId()), accountExternalKey);
    Map<String, UUID> map = getMeasureName(context.getAccountId(), context);
    List<RawUsageRecord> rawUsageRecordList = new ArrayList<>();
    List<OutputDTO> listDTO = client.getUsageAccount(startDate, endDate);
    for (int i = 0; i < listDTO.size(); i++) {

      RawUsageRecordImpl rawUsageRecordImpl = new RawUsageRecordImpl();

      try {
        rawUsageRecordImpl.setSubscriptionId(map.get(listDTO.get(i).getMeasureName()));

        rawUsageRecordImpl.setDate(getDateTimeFromMilis(listDTO.get(i).getSourceTimeInMillis()));
        rawUsageRecordImpl.setUnitType(listDTO.get(i).getMeasureName());
        rawUsageRecordImpl.setAmount(new BigDecimal(listDTO.get(i).getMeasureValue()));
        rawUsageRecordImpl.setTrackingId(listDTO.get(i).getSourceTimeInMillis());

        rawUsageRecordList.add(rawUsageRecordImpl);

      } catch (Exception e) {
        logger.error("{}", e.getMessage(), e);
      }
    }
    return rawUsageRecordList;
  }

  @Override
  public List<RawUsageRecord> getUsageForSubscription(
      UUID subscriptionId,
      DateTime startDate,
      DateTime endDate,
      UsageContext context,
      Iterable<PluginProperty> properties) {
    logger.info("[getUsageForSubscription]");
    TenantContext tenantContext =
        new PluginTenantContext(context.getAccountId(), context.getTenantId());

    // When making a call, the dates cannot be null and the end date cannot be earlier than the
    // start date. If any of these criteria is not met, then it returns an empty collection.
    if (startDate == null || endDate == null) {
      logger.error("Dates cannot be null!");
      return Collections.emptyList();
    } else if (startDate.compareTo(endDate) > 0) {
      logger.error("End date cannot be sooner than start date!");
      return Collections.emptyList();
    }

    startDate = sanitizeDateTime(startDate, true);
    endDate = sanitizeDateTime(endDate, false);

    String customField = getCustomFieldFromSubscription(subscriptionId, tenantContext);
    if (customField == null || customField.isEmpty()) {
      logger.error("Custom field measure_name is not found");
      return Collections.emptyList();
    }

    String externalAccountId = getExternalKeyFromAccount(context.getAccountId(), tenantContext);
    AmberfloClientImpl client =
        new AmberfloClientImpl(
            usageConfigurationHandler.getConfigurable(context.getTenantId()),
            externalAccountId,
            customField);

    List<RawUsageRecord> rawUsageRecordList = new ArrayList<>();
    List<OutputDTO> listDTO = client.getUsageSubscription(startDate, endDate);
    for (int i = 0; i < listDTO.size(); i++) {

      RawUsageRecordImpl rawUsageRecordImpl = new RawUsageRecordImpl();

      try {
        rawUsageRecordImpl.setSubscriptionId(subscriptionId);

        rawUsageRecordImpl.setDate(getDateTimeFromMilis(listDTO.get(i).getSourceTimeInMillis()));
        rawUsageRecordImpl.setUnitType(listDTO.get(i).getMeasureName());
        rawUsageRecordImpl.setAmount(new BigDecimal(listDTO.get(i).getMeasureValue()));
        rawUsageRecordImpl.setTrackingId(listDTO.get(i).getSourceTimeInMillis());

        rawUsageRecordList.add(rawUsageRecordImpl);
      } catch (Exception e) {
        logger.error("{}", e.getMessage(), e);
      }
    }
    return rawUsageRecordList;
  }

  private String getExternalKeyFromAccount(UUID accountId, TenantContext context) {
    try {
      return killbillAPI.getAccountUserApi().getAccountById(accountId, context).getExternalKey();
    } catch (AccountApiException e) {
      logger.error("{}", e.getMessage(), e);
    }
    return null;
  }

  private DateTime getDateTimeFromMilis(String milis) {
    Date date = new Date(Long.parseLong(milis));
    return new DateTime(date);
  }

  private String getCustomFieldFromSubscription(UUID subscriptionId, TenantContext context) {
    List<CustomField> customFields =
        killbillAPI
            .getCustomFieldUserApi()
            .getCustomFieldsForObject(subscriptionId, ObjectType.SUBSCRIPTION, context);

    for (CustomField customField : customFields) {
      if (customField.getFieldName().equals("measure_name")) return customField.getFieldValue();
    }

    return null;
  }

  private Map<String, UUID> getMeasureName(UUID accountId, TenantContext context) {
    try {
      List<SubscriptionBundle> subscriptionBundles =
          killbillAPI.getSubscriptionApi().getSubscriptionBundlesForAccountId(accountId, context);
      List<Subscription> subscriptions =
          subscriptionBundles.stream()
              .flatMap(p -> p.getSubscriptions().stream())
              .collect(Collectors.toList());
      Map<String, UUID> map = new HashMap<>();
      for (Subscription subscription : subscriptions) {
        if (getCustomFieldFromSubscription(subscription.getId(), context) != null) {
          map.put(
              getCustomFieldFromSubscription(subscription.getId(), context), subscription.getId());
        }
      }
      return map;

    } catch (SubscriptionApiException e) {
      logger.error("{}", e.getMessage(), e);
      return Collections.emptyMap();
    }
  }

  private DateTime sanitizeDateTime(DateTime time, boolean isStartDate) {

    // Due to how verification works in AmberFlo, future dates are not valid so there are some
    // checks in place to make sure proper calls are being made. A valid date is one that is not in
    // the future.

    if (isStartDate) {

      // If the start date is after the current possible time, then it returns the start of the
      // current day the request was made.
      if (time.isAfterNow()) {
        return DateTime.now().toLocalDate().toDateTimeAtStartOfDay();
      } else {

        // If a valid date has been given then then it returns the time given after previous checks.
        return time;
      }
    } else {

      // If the end date is after the current possible time, then it returns the end of the current
      // day the request was made
      if (time.isAfterNow()) {
        return DateTime.now();
      } else {

        // If a valid date has been given then then it returns the time given after previous checks.
        return time;
      }
    }
  }
}
