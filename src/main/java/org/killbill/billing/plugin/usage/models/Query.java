package org.killbill.billing.plugin.usage.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Query {

  private String customerId;
  private String eventId;
  private String meterApiName;
  private TimeRange timeRange;
  private Map<String, String> dimensions;
  private String limit;
  private String isAscending;
  private String pageSize;
  private String nextPageToken;
}
