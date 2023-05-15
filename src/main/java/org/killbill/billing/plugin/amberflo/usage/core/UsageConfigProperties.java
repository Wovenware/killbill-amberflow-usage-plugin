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

import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsageConfigProperties {

  private static final String PROPERTY_PREFIX = "org.killbill.billing.plugin.amberflo.usage.";

  public static final String AMBERFLO_KB_URL = "AMBERFLOW_KB_URL";

  private final String region;

  private String username;

  private String password;

  public static final String AMBERFLO_KB_APIKEY = "AMBERFLO_KB_APIKEY";

  public static final String DEFAULT_APIKEY = "";

  // Properties
  private String apiKey;
  private String ingestionFrequencySeconds;
  private String domain;
  private String ingestionBatchSize;
  private String isDebug;
  private String url;

  public UsageConfigProperties(final Properties properties, final String killBillRegion) {
    this.region = killBillRegion;

    this.url = properties.getProperty(PROPERTY_PREFIX + "url");
    this.username = properties.getProperty(PROPERTY_PREFIX + "username");
    this.password = properties.getProperty(PROPERTY_PREFIX + "password");
    this.apiKey = properties.getProperty(PROPERTY_PREFIX + "apiKey");
    this.ingestionFrequencySeconds =
        properties.getProperty(PROPERTY_PREFIX + "ingestionFrequencySeconds");
    this.domain = properties.getProperty(PROPERTY_PREFIX + "Domain");
    this.ingestionBatchSize = properties.getProperty(PROPERTY_PREFIX + "ingestionBatchSize");
    this.isDebug = properties.getProperty(PROPERTY_PREFIX + "isDebug");
  }

  public String getURL() {
    if (url == null || url.isEmpty()) {
      return getEnvironmentVariable(AMBERFLO_KB_URL, "https://app.amberflo.io/usage/events");
    }

    return url;
  }

  public String getApiKey() {
    if (apiKey == null || apiKey.isEmpty()) {
      return getEnvironmentVariable(AMBERFLO_KB_APIKEY, DEFAULT_APIKEY);
    }
    return apiKey;
  }

  private String getEnvironmentVariable(String envKey, String defaultValue) {
    Map<String, String> env = System.getenv();

    String value = env.get(envKey);

    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return value;
  }
}
