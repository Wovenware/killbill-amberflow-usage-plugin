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

package org.killbill.billing.plugin.amberflo.usage.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.killbill.billing.plugin.amberflo.usage.api.OutputDTO;
import org.killbill.billing.plugin.amberflo.usage.api.model.Record;
import org.killbill.billing.plugin.amberflo.usage.core.UsageConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmberfloClientImpl implements AmberfloClient {
  private static final ObjectMapper recordMapper = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(AmberfloClientImpl.class);

  public static final String CUSTOMER_ID_FIELD = "customerId";
  public static final String MEASURE_VALUE_FIELD = "measure_value::double";
  public static final String SOURCE_TIME_IN_MILLIS_FIELD = "sourceTimeInMillis";
  public static final String MEASURE_NAME_FIELD = "measure_name";
  public static final String METER_API_NAME_FIELD = "meterApiName";
  private static final String START_TIME_FIELD = "startTimeInSeconds";
  private static final String END_TIME_FIELD = "endTimeInSeconds";

  private UsageConfigProperties props;
  private final String accountExternalKey;
  private String subscription;

  private static final CloseableHttpClient httpclient = HttpClients.createDefault();

  public AmberfloClientImpl(
      UsageConfigProperties props, String accountExternalKey, String subscription) {
    this.props = props;
    this.accountExternalKey = accountExternalKey;
    this.subscription = subscription;
  }

  public AmberfloClientImpl(UsageConfigProperties props, String accountExternalKey) {
    super();
    this.props = props;
    this.accountExternalKey = accountExternalKey;
  }

  private List<OutputDTO> getUsageList(
      String accountExternalKey, String subscriptionName, String startDate, String endDate)
      throws Exception {

    List<OutputDTO> outputDTOList = new ArrayList<>();

    HttpGet httpget = new HttpGet(props.getURL());
    URI uri = null;
    if (subscriptionName == null) {
      uri =
          new URIBuilder(httpget.getURI())
              .addParameter(CUSTOMER_ID_FIELD, accountExternalKey)
              .addParameter(START_TIME_FIELD, startDate)
              .addParameter(END_TIME_FIELD, endDate)
              .build();
    } else {

      uri =
          new URIBuilder(httpget.getURI())
              .addParameter(CUSTOMER_ID_FIELD, accountExternalKey)
              .addParameter(METER_API_NAME_FIELD, subscriptionName)
              .addParameter(START_TIME_FIELD, startDate)
              .addParameter(END_TIME_FIELD, endDate)
              .build();
    }

    httpget.setURI(uri);
    httpget.setHeader("accept", "application/json");
    httpget.setHeader("x-api-key", props.getApiKey());
    HttpResponse httpresponse = httpclient.execute(httpget);

    Record clientRecord =
        recordMapper.readValue(httpresponse.getEntity().getContent(), Record.class);

    OutputDTO outputDTO = new OutputDTO();
    String[][] rows = clientRecord.getRows();
    if (clientRecord.getColumns() == null) {
      return outputDTOList;
    }
    final int customerIdCol = findInColumns(clientRecord.getColumns(), CUSTOMER_ID_FIELD);
    final int measureValueCol = findInColumns(clientRecord.getColumns(), MEASURE_VALUE_FIELD);
    final int sourceTimeInMillisCol =
        findInColumns(clientRecord.getColumns(), SOURCE_TIME_IN_MILLIS_FIELD);
    final int measureNameCol = findInColumns(clientRecord.getColumns(), MEASURE_NAME_FIELD);

    for (int i = 0; i < clientRecord.getRows().length; i++) {

      outputDTO.setCustomerID(rows[i][customerIdCol]);
      outputDTO.setSourceTimeInMillis(rows[i][sourceTimeInMillisCol]);
      outputDTO.setMeasureName(rows[i][measureNameCol]);
      outputDTO.setMeasureValue(rows[i][measureValueCol]);

      outputDTOList.add(outputDTO);
    }

    return outputDTOList;
  }

  // Finds specific item in array given array and the term to search, returns int
  // -1 if nothing is found
  public int findInColumns(String[] columns, String termToFind) {

    for (int i = 0; i < columns.length; i++) {
      if (termToFind.equals(columns[i])) {
        return i;
      }
    }

    return -1;
  }

  @Override
  public List<OutputDTO> getUsageAccount(DateTime startDate, DateTime endDate) {
    try {

      return getUsageList(
          accountExternalKey,
          null,
          Long.toString(startDate.getMillis() / 1000),
          Long.toString(endDate.getMillis() / 1000));
    } catch (Exception e) {
      logger.error("{}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<OutputDTO> getUsageSubscription(DateTime startDate, DateTime endDate) {
    List<OutputDTO> outputDTOList;
    try {
      outputDTOList =
          getUsageList(
              accountExternalKey,
              subscription,
              Long.toString(startDate.getMillis() / 1000),
              Long.toString(endDate.getMillis() / 1000));
      return outputDTOList;
    } catch (Exception e) {
      logger.error("{}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }
}
