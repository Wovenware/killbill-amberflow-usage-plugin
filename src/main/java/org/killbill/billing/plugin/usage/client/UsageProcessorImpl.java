package org.killbill.billing.plugin.usage.client;

import org.killbill.billing.plugin.usage.api.OutputDTO;

public class UsageProcessorImpl implements UsageProcessor {

  private UsageClientImpl client;

  public UsageProcessorImpl(UsageClientImpl client) {
    super();
    this.client = client;
  }

  @Override
  public OutputDTO getUsageAccount() {
    // client.queryTheAllApi(null);
    return null;
  }

  @Override
  public OutputDTO getUsageSubscription() {
    // client.queryTheAllApi(null);
    return null;
  }
}
