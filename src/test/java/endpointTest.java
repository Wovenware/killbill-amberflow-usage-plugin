import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Scanner;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.killbill.billing.plugin.usage.models.Record;

public class endpointTest {

  private String url = "https://app.amberflo.io/usage/events";

  @Test
  public void getUsageList() throws Exception {

    CloseableHttpClient httpclient = HttpClients.createDefault();

    try {

      HttpGet httpget = new HttpGet(url);

      httpget.setHeader("accept", "application/json");
      httpget.setHeader(
          "X-API-KEY",
          "e9d8dda0-ca7d-11ed-bb8c-49b9c8c4d9e0"); // "e9d8dda0-ca7d-11ed-bb8c-49b9c8c4d9e0"

      HttpResponse httpresponse = httpclient.execute(httpget);

      Scanner sc = new Scanner(httpresponse.getEntity().getContent());

      // Printing the status line
      System.out.println(httpresponse.getStatusLine());

      while (sc.hasNext()) {
        System.out.println(sc.nextLine());
      }

    } finally {
      httpclient.close();
    }
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void getListAllProductPlans() throws ClientProtocolException, IOException {

    CloseableHttpClient httpclient = HttpClients.createDefault();

    HttpGet httpget = new HttpGet(url);

    httpget.setHeader("accept", "application/json");
    httpget.setHeader("X-API-KEY", "e9d8dda0-ca7d-11ed-bb8c-49b9c8c4d9e0");

    HttpResponse httpresponse = httpclient.execute(httpget);

    Scanner sc = new Scanner(httpresponse.getEntity().getContent());

    Record plans = mapper.readValue(httpresponse.getEntity().getContent(), Record.class);
    System.out.println(plans.getNextPageToken());

    // Get measure_value::double, sourceTimeInMillis, customerID. Iterate over array
    // until name matches, order might change cannot hardcode specific position
    //
    while (sc.hasNext()) {
      System.out.println(sc.nextLine());
    }

    String[] columns = plans.getColumns();
    String termToFind = "customerId";
    int termPosition = -1;
    String termToFind2 = "a35132c1-3d28-45e3-ae98-101ea4211663";

    for (int i = 0; i < columns.length; i++) {

      if (termToFind.equals(columns[i])) {
        System.out.println("found! position is: " + i);
        termPosition = i;
      }
    }

    String[][] rows = plans.getRows();

    System.out.println(rows.length);

    for (int i = 0; i < rows.length; i++) {

      if (termToFind2.equals(rows[i][termPosition])) {
        System.out.println("found!" + rows[i][termPosition]);
        break;
      }
    }

    System.out.println("End of test");
  }

  @Test
  public void getListAllProductItemPrices() throws ClientProtocolException, IOException {

    CloseableHttpClient httpclient = HttpClients.createDefault();

    HttpGet httpget = new HttpGet(url);

    httpget.setHeader("accept", "application/json");
    httpget.setHeader("X-API-KEY", "e9d8dda0-ca7d-11ed-bb8c-49b9c8c4d9e0");

    HttpResponse httpresponse = httpclient.execute(httpget);

    Scanner sc = new Scanner(httpresponse.getEntity().getContent());

    // Printing the status line
    System.out.println(httpresponse.getStatusLine());

    while (sc.hasNext()) {
      System.out.println(sc.nextLine());
    }
  }

  @Test
  public void getAllProductItems() throws ClientProtocolException, IOException {

    CloseableHttpClient httpclient = HttpClients.createDefault();

    HttpGet httpget = new HttpGet(url);

    httpget.setHeader("accept", "application/json");
    httpget.setHeader("X-API-KEY", "e9d8dda0-ca7d-11ed-bb8c-49b9c8c4d9e0");

    HttpResponse httpresponse = httpclient.execute(httpget);

    Scanner sc = new Scanner(httpresponse.getEntity().getContent());

    // Printing the status line
    System.out.println(httpresponse.getStatusLine());

    while (sc.hasNext()) {
      System.out.println(sc.nextLine());
    }
  }

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
