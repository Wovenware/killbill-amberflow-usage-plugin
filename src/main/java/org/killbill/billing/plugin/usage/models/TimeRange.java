package org.killbill.billing.plugin.usage.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeRange {
  private String startTimeInSeconds;
  private String endTimeInSeconds;
}
