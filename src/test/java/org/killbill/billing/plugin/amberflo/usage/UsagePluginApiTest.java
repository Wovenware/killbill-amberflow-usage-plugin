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

package org.killbill.billing.plugin.amberflo.usage;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.killbill.billing.invoice.api.DryRunType;
import org.killbill.billing.usage.api.RawUsageRecord;
import org.killbill.billing.usage.plugin.api.UsageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class UsagePluginApiTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(UsagePluginApiTest.class);

  @Test
  public void testCreateGetUsageForSubscription() {
    logger.info("[usage-plugin] testCreateGetUsageForSubscription");
    UsageContext usageContext = new UsageContextImpl(context.getAccountId(), context.getTenantId());

    List<RawUsageRecord> rawRecord =
        usagePluginApiImpl.getUsageForSubscription(
            UUID.fromString("a35132c1-3d28-45e3-ae98-101ea4211663"),
            DateTime.parse("2023-04-01"),
            DateTime.parse("2023-04-30"),
            usageContext,
            null);

    String expectedUnitType = "BulletsAPI";
    UUID expectedSubId = UUID.fromString("a35132c1-3d28-45e3-ae98-101ea4211663");
    String expectedAmount = "10.0";
    // Assert

    if (!rawRecord.isEmpty()) {
      Assert.assertEquals(
          BigDecimal.valueOf(Double.valueOf(expectedAmount)), rawRecord.get(0).getAmount());
      Assert.assertEquals(expectedUnitType, rawRecord.get(0).getUnitType());
      Assert.assertEquals(expectedSubId, rawRecord.get(0).getSubscriptionId());
    }
  }
}

@Setter
class UsageContextImpl implements UsageContext {

  public UsageContextImpl(UUID accountId, UUID tenantId) {
    super();
    this.accountId = accountId;
    this.tenantId = tenantId;
  }

  private UUID accountId;
  private UUID tenantId;

  @Override
  public UUID getAccountId() {
    return accountId;
  }

  @Override
  public UUID getTenantId() {
    return tenantId;
  }

  @Override
  public DryRunType getDryRunType() {
    return DryRunType.TARGET_DATE;
  }

  @Override
  public LocalDate getInputTargetDate() {
    return LocalDate.now();
  }
}
