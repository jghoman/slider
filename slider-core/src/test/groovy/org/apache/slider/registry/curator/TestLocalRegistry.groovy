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

package org.apache.slider.registry.curator

import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hoya.HoyaKeys
import org.apache.hoya.yarn.MicroZKCluster
import org.apache.slider.core.registry.ServiceInstanceData
import org.apache.slider.server.services.curator.CuratorHelper
import org.apache.slider.server.services.curator.RegistryBinderService
import org.junit.After
import org.junit.Before
import org.junit.Test

class TestLocalRegistry {
  MicroZKCluster miniZK
  CuratorHelper curatorHelper
  RegistryBinderService<ServiceInstanceData> registryBinder

  @Before
  void setup() {
    miniZK = new MicroZKCluster()
    miniZK.createCluster()

    def conf = new YarnConfiguration()
    CuratorHelper curatorHelper =
        new CuratorHelper(conf, miniZK.zkBindingString);
    registryBinder = curatorHelper.createRegistryBinderService("/services");
    registryBinder.init(conf)
    registryBinder.start()
  }

  @After
  void teardown() {
    registryBinder?.stop()
    miniZK?.close()
  }


  @Test
  public void testRegisterAndQuery() throws Throwable {
    registryBinder.register(HoyaKeys.APP_TYPE, "instance3",
        new URL("http", "localhost", 80, "/"),
        null)
    def instance = registryBinder.queryForInstance(
        HoyaKeys.APP_TYPE,
        "instance3")
    assert instance != null
  }

  @Test
  public void testRegisterAndList() throws Throwable {
    registryBinder.register(HoyaKeys.APP_TYPE, "instance3",
        new URL("http", "localhost", 80, "/"),
        null)
    registryBinder.register(HoyaKeys.APP_TYPE, "instance2",
        new URL("http", "localhost", 8090, "/"),
        null)
    def instances = registryBinder.instanceIDs(HoyaKeys.APP_TYPE)
    assert instances.size() ==2
    def instance = registryBinder.queryForInstance(
        HoyaKeys.APP_TYPE,
        "instance3")
    assert instance != null
  }

}
