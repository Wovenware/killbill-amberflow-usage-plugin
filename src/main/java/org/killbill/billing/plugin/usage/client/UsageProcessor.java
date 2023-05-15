package org.killbill.billing.plugin.usage.client;

import org.killbill.billing.plugin.usage.api.OutputDTO;

public interface UsageProcessor {

  OutputDTO getUsageAccount();

  OutputDTO getUsageSubscription();
}
