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

package org.killbill.billing.plugin.usage.core;

import java.util.Map;
import java.util.Properties;

public class UsageConfigProperties {

  private static final String PROPERTY_PREFIX = "org.killbill.billing.plugin.usage.";

  public static final String HELLOWORLD_KB_USERNAME = "HELLOWORLD_KB_USERNAME";
  public static final String HELLOWORLD_KB_PASSWORD = "HELLOWORLD_KB_PASSWORD";

  private final String region;

  private String username;

  private String password;

  public static final String AMBERFLO_KB_APIKEY = "AMBERFLOW_KB_APIKEY";
  public static final String AMBERFLO_KB_DOMAIN = "AMBERFLOW_KB_DOMAIN";
  public static final String AMBERFLO_KB_INGESTION_FREQUENCY_SECONDS =
      "AMBERFLOW_KB_INGESTION_FREQUENCY_SECONDS";
  public static final String AMBERFLO_KB_INGESTION_BATCH_SIZE = "AMBERFLOW_KB_INGESTION_BATCH_SIZE";
  public static final String AMBERFLO_KB_IS_DEBUG = "AMBERFLOW_KB_IS_DEBUG";

  public static final String DEFAULT_REGION = "US_West";
  public static final String DEFAULT_APIKEY = "";
  public static final String DEFAULT_DOMAIN = "Dev";
  public static final String DEFAULT_INGESTION_FREQUENCY_SECONDS = "1";
  public static final String DEFAULT_INGESTION_BATCH_SIZE = "10";
  public static final String DEFAULT_IS_DEBUG = "true";

  // Properties
  private String apiKey;
  private String ingestionFrequencySeconds;
  private String domain;
  private String ingestionBatchSize;
  private String isDebug;

  public UsageConfigProperties(final Properties properties, final String killBillRegion) {
    this.region = killBillRegion;

    this.username = properties.getProperty(PROPERTY_PREFIX + "username");
    this.password = properties.getProperty(PROPERTY_PREFIX + "password");
    this.apiKey = properties.getProperty(PROPERTY_PREFIX + "apiKey");
    this.ingestionFrequencySeconds =
        properties.getProperty(PROPERTY_PREFIX + "ingestionFrequencySeconds");
    this.domain = properties.getProperty(PROPERTY_PREFIX + "Domain");
    this.ingestionBatchSize = properties.getProperty(PROPERTY_PREFIX + "ingestionBatchSize");
    this.isDebug = properties.getProperty(PROPERTY_PREFIX + "isDebug");
  }

  public String getRegion() {

    return region;
  }

  public String getUsername() {
    if (username == null || username.isEmpty()) {
      return getClient(HELLOWORLD_KB_USERNAME, "test");
    }

    return username;
  }

  public String getPassword() {
    if (password == null || password.isEmpty()) {
      return getClient(HELLOWORLD_KB_PASSWORD, "test");
    }

    return password;
  }

  public String getApiKey() {
    if (apiKey == null || apiKey.isEmpty()) {
      return getClient(AMBERFLO_KB_APIKEY, DEFAULT_APIKEY);
    }
    return apiKey;
  }

  public String getIngestionFrequencySeconds() {
    if (ingestionFrequencySeconds == null || ingestionFrequencySeconds.isEmpty()) {
      return getClient(
          AMBERFLO_KB_INGESTION_FREQUENCY_SECONDS, DEFAULT_INGESTION_FREQUENCY_SECONDS);
    }
    return ingestionFrequencySeconds;
  }

  public String getDomain() {
    if (domain == null || domain.isEmpty()) {
      return getClient(AMBERFLO_KB_DOMAIN, DEFAULT_DOMAIN);
    }
    return domain;
  }

  public String getIngestionBatchSize() {
    if (ingestionBatchSize == null || ingestionBatchSize.isEmpty()) {
      return getClient(AMBERFLO_KB_INGESTION_BATCH_SIZE, DEFAULT_INGESTION_BATCH_SIZE);
    }
    return ingestionBatchSize;
  }

  public String getIsDebug() {
    if (isDebug == null || isDebug.isEmpty()) {
      return getClient(AMBERFLO_KB_IS_DEBUG, DEFAULT_IS_DEBUG);
    }
    return isDebug;
  }

  private String getClient(String envKey, String defaultValue) {
    Map<String, String> env = System.getenv();

    String value = env.get(envKey);

    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return value;
  }
}
