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

import java.util.List;
import org.joda.time.DateTime;
import org.killbill.billing.plugin.amberflo.usage.api.OutputDTO;

public interface AmberfloClient {

  /*
   * The following methods require a start date and end date to fetch the usage
   * pertaining to the relevant account. A valid date is in the format YYYY-MM-DD
   * with the start date being before the end date chronologically. The end date
   * must not exceed the current date due to restricitons with Amberflo even
   * though it is ok to do so with Kill Bill.
   *
   * There are checks in place for these scenarios to ensure proper functionality.
   *
   *
   * @param startDate Starting value of the range to verify usage of a specified
   * account in the format YYYY-MM-DD
   *
   * @param endDate Starting value of the range to verify usage of a specified
   * account in the format YYYY-MM-DD
   *
   * @return Returns a list using the OutputDTO model that contains: customerID,
   * measureValue, sourceTimeInMillis, and measureName
   */

  List<OutputDTO> getUsageAccount(DateTime startSate, DateTime endDate);

  List<OutputDTO> getUsageSubscription(DateTime startSate, DateTime endDate);
}
