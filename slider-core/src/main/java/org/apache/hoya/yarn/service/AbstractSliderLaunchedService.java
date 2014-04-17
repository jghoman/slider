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

package org.apache.hoya.yarn.service;


import org.apache.hadoop.yarn.conf.YarnConfiguration;
import static org.apache.hoya.HoyaXmlConfKeys.*;
import org.apache.hoya.exceptions.BadConfigException;
import org.apache.slider.core.registry.info.ServiceInstanceData;
import org.apache.slider.server.services.curator.CuratorHelper;
import org.apache.slider.server.services.curator.RegistryBinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base service for the standard slider client/server services
 */
public abstract class AbstractSliderLaunchedService extends
                                                    CompoundLaunchedService {
  private static final Logger log =
    LoggerFactory.getLogger(AbstractSliderLaunchedService.class);

  public AbstractSliderLaunchedService(String name) {
    super(name);
    // make sure all the yarn configs get loaded
    new YarnConfiguration();
  }

  protected RegistryBinderService<ServiceInstanceData> startRegistrationService()
      throws BadConfigException {
    String zkConnection = getConfig().get(REGISTRY_ZK_QUORUM,
      DEFAULT_REGISTRY_ZK_QUORUM);
    String zkPath = getConfig().get(REGISTRY_PATH, DEFAULT_REGISTRY_PATH);
    return startRegistrationService(zkConnection, zkPath);
  }

  /**
   * Start the registration service
   * @param zkConnection
   * @param zkPath
   * @return
   */
  public RegistryBinderService<ServiceInstanceData> startRegistrationService(
    String zkConnection, String zkPath) {
    CuratorHelper curatorHelper =
      new CuratorHelper(getConfig(), zkConnection);

    //registry will start curator as well as the binder, in the correct order
    RegistryBinderService<ServiceInstanceData> registryBinderService =
      curatorHelper.createRegistryBinderService(zkPath);
    boolean started = deployChildService(registryBinderService);
    return registryBinderService;
  }


}
