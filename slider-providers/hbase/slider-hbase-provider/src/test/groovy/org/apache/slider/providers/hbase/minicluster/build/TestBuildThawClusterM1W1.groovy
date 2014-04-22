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
import org.apache.slider.providers.hbase.HBaseKeys
import org.apache.hoya.yarn.HoyaActions
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.slider.providers.hbase.minicluster.HBaseMiniClusterTestBase
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.junit.Test

import static HBaseKeys.PROVIDER_HBASE
import static org.apache.hoya.yarn.Arguments.ARG_PROVIDER

@CompileStatic
@Slf4j

class TestBuildThawClusterM1W1 extends HBaseMiniClusterTestBase {

  @Test
  public void test_build_thaw_cluster_m1_w1() throws Throwable {
    String clustername = "test_build_thaw_cluster_m1_w1"
    createMiniCluster(clustername, getConfiguration(), 1, true)

    describe "verify that a built cluster can be thawed"

    ServiceLauncher launcher = createOrBuildHoyaCluster(
        HoyaActions.ACTION_BUILD,
        clustername,
        [
            (HBaseKeys.ROLE_MASTER): 1,
            (HBaseKeys.ROLE_WORKER): 1,
        ],
        [
            ARG_PROVIDER, PROVIDER_HBASE
        ],
        true,
        false,
        [:])
    HoyaClient hoyaClient = (HoyaClient) launcher.service
    addToTeardown(hoyaClient);
    def serviceRegistryClient = hoyaClient.YARNRegistryClient
    ApplicationReport report = serviceRegistryClient.findInstance(clustername)
    assert report == null;

    //thaw time
    ServiceLauncher l2 = thawHoyaCluster(clustername, [], true)
    HoyaClient client2 = (HoyaClient) l2.service
    addToTeardown(client2);
    waitForClusterLive(l2.service as HoyaClient)
  }

}
