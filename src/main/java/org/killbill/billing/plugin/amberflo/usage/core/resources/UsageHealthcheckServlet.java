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

package org.killbill.billing.plugin.amberflo.usage.core.resources;

import com.google.inject.Inject;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooby.Result;
import org.jooby.mvc.GET;
import org.jooby.mvc.Local;
import org.jooby.mvc.Path;
import org.killbill.billing.plugin.amberflo.usage.core.UsageHealthcheck;
import org.killbill.billing.plugin.core.resources.PluginHealthcheck;
import org.killbill.billing.tenant.api.Tenant;

@Singleton
@Path("/healthcheck")
public class UsageHealthcheckServlet extends PluginHealthcheck {

  private final UsageHealthcheck healthcheck;

  @Inject
  public UsageHealthcheckServlet(final UsageHealthcheck healthcheck) {
    this.healthcheck = healthcheck;
  }

  @GET
  public Result check(@Local @Named("killbill_tenant") final Optional<Tenant> tenant) {
    return check(healthcheck, tenant.orElse(null), null);
  }
}
