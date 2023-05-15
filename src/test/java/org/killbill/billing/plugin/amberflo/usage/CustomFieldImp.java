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

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.killbill.billing.ObjectType;
import org.killbill.billing.util.customfield.CustomField;

@Getter
@Setter
public class CustomFieldImp implements CustomField {

  protected DateTime createdDate;
  protected String fieldName;
  protected String fieldValue;
  protected UUID id;
  protected UUID objectId;
  protected ObjectType objectType;
  protected DateTime updatedDate;

  public CustomFieldImp() {}
}
