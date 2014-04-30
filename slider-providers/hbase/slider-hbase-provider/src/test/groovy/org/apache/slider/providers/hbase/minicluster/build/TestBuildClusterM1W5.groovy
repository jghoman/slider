/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.slider.providers.hbase.minicluster.build

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.yarn.service.launcher.LauncherExitCodes
import org.apache.hoya.HoyaExitCodes
import org.apache.hoya.exceptions.SliderException
import org.apache.slider.providers.hbase.HBaseKeys
import org.apache.hoya.yarn.HoyaActions
import org.apache.hoya.yarn.client.SliderClient
import org.apache.slider.providers.hbase.minicluster.HBaseMiniClusterTestBase
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.junit.Test

import static HBaseKeys.PROVIDER_HBASE
import static org.apache.hoya.yarn.Arguments.ARG_PROVIDER

@CompileStatic
@Slf4j

class TestBuildClusterM1W5 extends HBaseMiniClusterTestBase {

  @Test
  public void testBuildCluster() throws Throwable {
    String clustername = "test_build_cluster_m1_w5"
    createMiniCluster(clustername, getConfiguration(), 1, true)

    describe "verify that a build cluster is created but not started"

    ServiceLauncher launcher = createOrBuildCluster(
        HoyaActions.ACTION_BUILD,
        clustername,
        [
            (HBaseKeys.ROLE_MASTER): 1,
            (HBaseKeys.ROLE_WORKER): 5,
        ],
        [
            ARG_PROVIDER, PROVIDER_HBASE
        ],
        true,
        false,
        [:])
    SliderClient hoyaClient = (SliderClient) launcher.service
    addToTeardown(hoyaClient);

    //verify that exists(live) is now false
    assert LauncherExitCodes.EXIT_FALSE ==
           hoyaClient.actionExists(clustername, true)

    //but the cluster is still there for the default
    assert 0 == hoyaClient.actionExists(clustername, false)

    def serviceRegistryClient = hoyaClient.YARNRegistryClient
    ApplicationReport report = serviceRegistryClient.findInstance(clustername)
    assert report == null;

    //and a second attempt will fail as the cluster now exists
    try {
      createOrBuildCluster(
          HoyaActions.ACTION_BUILD,
          clustername,
          [
              (HBaseKeys.ROLE_MASTER): 1,
              (HBaseKeys.ROLE_WORKER): 3,
          ],
          [
              ARG_PROVIDER, PROVIDER_HBASE
          ],
          true,
          false,
          [:])
    } catch (SliderException e) {
      assert e.exitCode == HoyaExitCodes.EXIT_INSTANCE_EXISTS
    }
  }

}
