/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.slider.server.services.curator;

import java.util.Locale;

public class RegistryNaming {

  public static String SLIDER_UUID = "b71ff114-37f7-45e7-99d2-f95efb81f2f9";

  public static String SLIDER_INSTANCE_NAME_FORMAT =
    SLIDER_UUID + "-%s-%s";

  public static String createUniqueInstanceId(String instanceName,
                                      String userName,
                                      String serviceName) {
    return String.format(Locale.ENGLISH, SLIDER_INSTANCE_NAME_FORMAT, userName,
                         instanceName);
  }
}
