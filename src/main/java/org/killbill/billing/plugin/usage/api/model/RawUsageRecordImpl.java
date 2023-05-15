package org.killbill.billing.plugin.usage.api.model;

import java.math.BigDecimal;
import java.util.UUID;
import org.joda.time.DateTime;
import org.killbill.billing.usage.api.RawUsageRecord;

public class RawUsageRecordImpl implements RawUsageRecord {

  @Override
  public UUID getSubscriptionId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DateTime getDate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getUnitType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BigDecimal getAmount() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTrackingId() {
    // TODO Auto-generated method stub
    return null;
  }
}
