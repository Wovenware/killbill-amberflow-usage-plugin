package org.killbill.billing.plugin.usage.client;

import com.amberflo.metering.usage.clients.UsageClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.killbill.billing.plugin.usage.api.OutputDTO;
import org.killbill.billing.plugin.usage.core.UsageConfigProperties;
import org.killbill.billing.plugin.usage.models.Record;

public class UsageClientImpl {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final boolean ASCENDING = true;

  public static final String CUSTOMER_ID_FIELD = "customerId";
  public static final String MEASURE_VALUE_FIELD = "measure_value::double";
  public static final String SOURCE_TIME_IN_MILLIS_FIELD = "sourceTimeInMillis";
  public static final String SUBSCRIPTION_FIELD = "subscription";

  private UsageConfigProperties props;
  private UsageClient usageClient;
  private String url = "https://app.amberflo.io/usage/events";

  public UsageClientImpl(UsageConfigProperties props) {
    this.props = props;
    final UsageClient usageClient = new UsageClient(props.getApiKey());
  }

  public List<OutputDTO> getUsageList() throws Exception {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    List<OutputDTO> outputDTOList = new ArrayList<>();

    try {

      HttpGet httpget = new HttpGet(url);

      httpget.setHeader("accept", "application/json");
      httpget.setHeader("X-API-KEY", props.getApiKey()); // "e9d8dda0-ca7d-11ed-bb8c-49b9c8c4d9e0"

      HttpResponse httpresponse = httpclient.execute(httpget);

      Record record = mapper.readValue(httpresponse.getEntity().getContent(), Record.class);

      OutputDTO outputDTO = new OutputDTO();
      String[][] rows = record.getRows();

      final int customerIdCol = findInColumns(record.getColumns(), CUSTOMER_ID_FIELD);
      final int measureValueCol = findInColumns(record.getColumns(), MEASURE_VALUE_FIELD);
      final int sourceTimeInMillisCol =
          findInColumns(record.getColumns(), SOURCE_TIME_IN_MILLIS_FIELD);
      final int subscriptionCol = findInColumns(record.getColumns(), SUBSCRIPTION_FIELD);

      for (int i = 0; i < record.getRows().length; i++) {

        outputDTO.setCustomerID(rows[i][customerIdCol]);
        outputDTO.setMeasureValue(rows[i][measureValueCol]);
        outputDTO.setSourceTimeInMillis(rows[i][sourceTimeInMillisCol]);
        outputDTO.setSubscription(rows[i][subscriptionCol]);

        outputDTOList.add(outputDTO);
      }

    } finally {
      httpclient.close();
    }

    return outputDTOList;
  }

  // Finds specific item in array given array and the term to search, returns int
  // -1 if nothing is found
  public int findInColumns(String[] columns, String termToFind) {

    for (int i = 0; i < columns.length; i++) {

      if (termToFind == columns[i]) return i;
    }

    return -1;
  }

  public int findInRows(String[][] rows, String termToFind, int property) {

    for (int i = 0; i < rows.length; i++) {

      if (termToFind == rows[i][property]) return i;
    }

    return -1;
  }
}
