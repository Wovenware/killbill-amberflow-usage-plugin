package org.killbill.billing.plugin.usage.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

  private String[] columns;
  private String[][] rows;
  private String nextPageToken;
  private Query query;
}
