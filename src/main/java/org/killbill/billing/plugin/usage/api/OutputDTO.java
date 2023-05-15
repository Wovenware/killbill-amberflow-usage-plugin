package org.killbill.billing.plugin.usage.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutputDTO {

  private String customerID;
  private String measureValue;
  private String sourceTimeInMillis;
  private String subscription;
}
