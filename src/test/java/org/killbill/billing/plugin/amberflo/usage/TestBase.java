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

package org.killbill.billing.plugin.amberflo.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountUserApi;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionApi;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.InvoiceUserApi;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.PaymentApi;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.plugin.TestUtils;
import org.killbill.billing.plugin.amberflo.usage.api.UsagePluginApiImpl;
import org.killbill.billing.plugin.amberflo.usage.core.UsageActivator;
import org.killbill.billing.plugin.amberflo.usage.core.UsageConfigProperties;
import org.killbill.billing.plugin.amberflo.usage.core.UsageConfigurationHandler;
import org.killbill.billing.usage.plugin.api.UsagePluginApi;
import org.killbill.billing.util.api.CustomFieldUserApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.customfield.CustomField;
import org.killbill.clock.ClockMock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("java:S1192")
public class TestBase {

  private static final String ENDPOINT_TEST_URL =
      "/?customerId=test&meterApiName=test&startTimeInSeconds=1680321600&endTimeInSeconds=1682827200";

  private static final String ENDPOINT_CUSTOMER_ID_URL =
      "/?customerId=a35132c1-3d28-45e3-ae98-101ea4211663";
  private static final String ENDPOINT_CUSTOMER_ID_AND_METER_API_URL =
      "/?customerId=a35132c1-3d28-45e3-ae98-101ea4211663&meterApiName=BulletsAPI";

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_DATA = "application/x-www-form-urlencoded";

  protected static final String PROPERTIES_FILE_NAME = "usage.properties";

  public static final Currency DEFAULT_CURRENCY = Currency.USD;
  public static final String DEFAULT_COUNTRY = "US";
  private WireMockServer wireMockServer;
  protected ClockMock clock;
  protected CallContext context;
  protected Account account;
  protected UsagePluginApiImpl usagePluginApiImpl;
  protected OSGIKillbillAPI killbillApi;
  protected CustomFieldUserApi customFieldUserApi;
  protected InvoiceUserApi invoiceUserApi;
  protected AccountUserApi accountUserApi;
  protected SubscriptionApi subscriptionApi;
  protected PaymentApi paymentApi;
  protected PaymentMethod paymentMethod;
  protected UsageConfigurationHandler usageConfigHandler;
  protected UsageConfigProperties usageConfigProperties;
  protected UsagePluginApi usagePluginApi;

  private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

  @Before
  public void setUp() throws Exception {
    setUpBeforeSuite();

    logger.info("[usage-plugin][setUp] initialization");

    clock = new ClockMock();
    context = Mockito.mock(CallContext.class);
    Mockito.when(context.getTenantId()).thenReturn(UUID.randomUUID());

    account = TestUtils.buildAccount(DEFAULT_CURRENCY, DEFAULT_COUNTRY);
    Mockito.when(account.getEmail()).thenReturn(UUID.randomUUID().toString() + "@example.com");
    killbillApi = TestUtils.buildOSGIKillbillAPI(account);
    customFieldUserApi = Mockito.mock(CustomFieldUserApi.class);
    Mockito.when(killbillApi.getCustomFieldUserApi()).thenReturn(customFieldUserApi);
    List<CustomField> customFields = new ArrayList<>();
    CustomFieldImp customField = new CustomFieldImp();
    customField.setFieldName("measure_name");
    customField.setFieldValue("test");
    customFields.add(customField);
    Mockito.when(
            killbillApi
                .getCustomFieldUserApi()
                .getCustomFieldsForObject(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(customFields);
    invoiceUserApi = Mockito.mock(InvoiceUserApi.class);
    Mockito.when(killbillApi.getInvoiceUserApi()).thenReturn(invoiceUserApi);
    accountUserApi = Mockito.mock(AccountUserApi.class);
    Mockito.when(killbillApi.getAccountUserApi()).thenReturn(accountUserApi);
    paymentApi = Mockito.mock(PaymentApi.class);
    Mockito.when(killbillApi.getPaymentApi()).thenReturn(paymentApi);
    paymentMethod = Mockito.mock(PaymentMethod.class);
    Mockito.when(
            paymentApi.getPaymentMethodById(
                Mockito.any(UUID.class),
                Mockito.any(Boolean.class),
                Mockito.any(Boolean.class),
                Mockito.anyList(),
                Mockito.any(TenantContext.class)))
        .thenReturn(paymentMethod);

    List<SubscriptionBundle> bundles = new ArrayList<>();

    subscriptionApi = Mockito.mock(SubscriptionApi.class);
    List<Subscription> subcriptions = new ArrayList<>();
    Subscription subscription =
        new SubscriptionImp(UUID.fromString("a35132c1-3d28-45e3-ae98-101ea4211663"), "test");

    subcriptions.add(subscription);
    SubscriptionBundle bundle = new SubscriptionBundleImp(subcriptions);
    bundles.add(bundle);
    Mockito.when(killbillApi.getSubscriptionApi()).thenReturn(subscriptionApi);
    Mockito.when(
            killbillApi
                .getSubscriptionApi()
                .getSubscriptionBundlesForAccountId(Mockito.any(), Mockito.any()))
        .thenReturn(bundles);

    usageConfigHandler =
        new UsageConfigurationHandler(null, UsageActivator.PLUGIN_NAME, killbillApi);

    final OSGIConfigPropertiesService configPropertiesService =
        Mockito.mock(OSGIConfigPropertiesService.class);
    usagePluginApiImpl = new UsagePluginApiImpl(usageConfigHandler, killbillApi);
    Mockito.when(killbillApi.getAccountUserApi().getAccountById(Mockito.any(), Mockito.any()))
        .thenReturn(account);
    Mockito.when(
            killbillApi
                .getAccountUserApi()
                .getAccountById(Mockito.any(), Mockito.any())
                .getExternalKey())
        .thenReturn("test");

    setUpIntegration(PROPERTIES_FILE_NAME);
  }

  protected void setUpIntegration(String fileName) throws IOException {
    logger.info("[usage-plugin][setUpIntegration] initialization");
    final Properties properties = TestUtils.loadProperties(fileName);
    usageConfigProperties = new UsageConfigProperties(properties, "");
    usageConfigHandler.setDefaultConfigurable(usageConfigProperties);
  }

  private void setUpBeforeSuite() throws IOException, SQLException {
    logger.info("[usage-plugin][setUpBeforeSuite] initialization");
    wireMockServer = new WireMockServer(wireMockConfig().port(7040));
    WireMock.configureFor("localhost", 7040);
    wireMockServer.start();

    getUsageFromHttp();
    getUsageWithCustomerId();
    getUsageWithCustomerIdAndMeter();
  }

  @After
  public void tearDownAfterSuite() throws IOException {
    wireMockServer.stop();
  }

  private void getUsageWithCustomerIdAndMeter() {
    stubFor(
        get(urlEqualTo(ENDPOINT_CUSTOMER_ID_AND_METER_API_URL))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, CONTENT_DATA)
                    .withBody(
                        "{\r\n"
                            + "  \"columns\": [\r\n"
                            + "    \"aflo.cancel_previous_resource_event\",\r\n"
                            + "    \"owner\",\r\n"
                            + "    \"sourceTimeInMillis\",\r\n"
                            + "    \"isFreshRecord\",\r\n"
                            + "    \"customerId\",\r\n"
                            + "    \"subscription\",\r\n"
                            + "    \"uniqueId\",\r\n"
                            + "    \"measure_name\",\r\n"
                            + "    \"time\",\r\n"
                            + "    \"measure_value::double\"\r\n"
                            + "  ],\r\n"
                            + "  \"rows\": [\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1680217660435\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      \"2\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-30 23:07:40.435000000\",\r\n"
                            + "      \"87.0\"\r\n"
                            + "    ]\r\n"
                            + "  ],\r\n"
                            + "  \"nextPageToken\": null,\r\n"
                            + "  \"query\": {\r\n"
                            + "    \"customerId\": \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "    \"eventId\": null,\r\n"
                            + "    \"meterApiName\": \"BulletsAPI\",\r\n"
                            + "    \"timeRange\": {\r\n"
                            + "      \"startTimeInSeconds\": 1630808537,\r\n"
                            + "      \"endTimeInSeconds\": null\r\n"
                            + "    },\r\n"
                            + "    \"dimensions\": {},\r\n"
                            + "    \"limit\": null,\r\n"
                            + "    \"isAscending\": false,\r\n"
                            + "    \"pageSize\": null,\r\n"
                            + "    \"nextPageToken\": null\r\n"
                            + "  }\r\n"
                            + "}")));
  }

  private void getUsageWithCustomerId() {

    stubFor(
        get(urlEqualTo(ENDPOINT_CUSTOMER_ID_URL))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, CONTENT_DATA)
                    .withBody(
                        "{\r\n"
                            + "  \"columns\": [\r\n"
                            + "    \"aflo.cancel_previous_resource_event\",\r\n"
                            + "    \"owner\",\r\n"
                            + "    \"sourceTimeInMillis\",\r\n"
                            + "    \"isFreshRecord\",\r\n"
                            + "    \"customerId\",\r\n"
                            + "    \"subscription\",\r\n"
                            + "    \"uniqueId\",\r\n"
                            + "    \"measure_name\",\r\n"
                            + "    \"time\",\r\n"
                            + "    \"measure_value::double\"\r\n"
                            + "  ],\r\n"
                            + "  \"rows\": [\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1680217660435\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      \"2\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-30 23:07:40.435000000\",\r\n"
                            + "      \"87.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1680203084267\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      null,\r\n"
                            + "      null,\r\n"
                            + "      \"RocksApi\",\r\n"
                            + "      \"2023-03-30 19:04:44.267000000\",\r\n"
                            + "      \"18.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1679938401005\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      null,\r\n"
                            + "      null,\r\n"
                            + "      \"RocksApi\",\r\n"
                            + "      \"2023-03-27 17:33:21.005000000\",\r\n"
                            + "      \"100.0\"\r\n"
                            + "    ]\r\n"
                            + "  ],\r\n"
                            + "  \"nextPageToken\": null,\r\n"
                            + "  \"query\": {\r\n"
                            + "    \"customerId\": \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "    \"eventId\": null,\r\n"
                            + "    \"meterApiName\": null,\r\n"
                            + "    \"timeRange\": {\r\n"
                            + "      \"startTimeInSeconds\": 1630807932,\r\n"
                            + "      \"endTimeInSeconds\": null\r\n"
                            + "    },\r\n"
                            + "    \"dimensions\": {},\r\n"
                            + "    \"limit\": null,\r\n"
                            + "    \"isAscending\": false,\r\n"
                            + "    \"pageSize\": null,\r\n"
                            + "    \"nextPageToken\": null\r\n"
                            + "  }\r\n"
                            + "}")));
  }

  private void getUsageFromHttp() {
    // ok
    stubFor(
        get(urlEqualTo(ENDPOINT_TEST_URL))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, CONTENT_DATA)
                    .withBody(
                        "{\r\n"
                            + "  \"columns\": [\r\n"
                            + "    \"aflo.cancel_previous_resource_event\",\r\n"
                            + "    \"owner\",\r\n"
                            + "    \"sourceTimeInMillis\",\r\n"
                            + "    \"isFreshRecord\",\r\n"
                            + "    \"customerId\",\r\n"
                            + "    \"subscription\",\r\n"
                            + "    \"uniqueId\",\r\n"
                            + "    \"measure_name\",\r\n"
                            + "    \"time\",\r\n"
                            + "    \"measure_value::double\"\r\n"
                            + "  ],\r\n"
                            + "  \"rows\": [\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1680217660435\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      \"2\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-30 23:07:40.435000000\",\r\n"
                            + "      \"87.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1680203084267\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      null,\r\n"
                            + "      null,\r\n"
                            + "      \"RocksApi\",\r\n"
                            + "      \"2023-03-30 19:04:44.267000000\",\r\n"
                            + "      \"18.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1679938401005\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"a35132c1-3d28-45e3-ae98-101ea4211663\",\r\n"
                            + "      null,\r\n"
                            + "      null,\r\n"
                            + "      \"RocksApi\",\r\n"
                            + "      \"2023-03-27 17:33:21.005000000\",\r\n"
                            + "      \"100.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1679938234609\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"ed00975d-567c-4edf-a02d-6fdfdf29edcd\",\r\n"
                            + "      \"123\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-27 17:30:34.609000000\",\r\n"
                            + "      \"1000.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1679938019365\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"ed00975d-567c-4edf-a02d-6fdfdf29edcd\",\r\n"
                            + "      \"123\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-27 17:26:59.365000000\",\r\n"
                            + "      \"1000.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1679937959100\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"ed00975d-567c-4edf-a02d-6fdfdf29edcd\",\r\n"
                            + "      \"123\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-27 17:25:59.100000000\",\r\n"
                            + "      \"40.0\"\r\n"
                            + "    ],\r\n"
                            + "    [\r\n"
                            + "      null,\r\n"
                            + "      \"17212\",\r\n"
                            + "      \"1679937878785\",\r\n"
                            + "      \"1\",\r\n"
                            + "      \"ed00975d-567c-4edf-a02d-6fdfdf29edcd\",\r\n"
                            + "      \"121\",\r\n"
                            + "      null,\r\n"
                            + "      \"BulletsAPI\",\r\n"
                            + "      \"2023-03-27 17:24:38.785000000\",\r\n"
                            + "      \"10.0\"\r\n"
                            + "    ]\r\n"
                            + "  ],\r\n"
                            + "  \"nextPageToken\": null,\r\n"
                            + "  \"query\": {\r\n"
                            + "    \"customerId\": null,\r\n"
                            + "    \"eventId\": null,\r\n"
                            + "    \"meterApiName\": null,\r\n"
                            + "    \"timeRange\": {\r\n"
                            + "      \"startTimeInSeconds\": 1630791031,\r\n"
                            + "      \"endTimeInSeconds\": null\r\n"
                            + "    },\r\n"
                            + "    \"dimensions\": {},\r\n"
                            + "    \"limit\": null,\r\n"
                            + "    \"isAscending\": false,\r\n"
                            + "    \"pageSize\": null,\r\n"
                            + "    \"nextPageToken\": null\r\n"
                            + "  }\r\n"
                            + "}")));
  }
}
