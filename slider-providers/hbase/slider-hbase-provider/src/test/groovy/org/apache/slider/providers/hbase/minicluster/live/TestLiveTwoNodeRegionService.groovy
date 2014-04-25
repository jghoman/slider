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

package org.apache.slider.providers.hbase.minicluster.live

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.hbase.ClusterStatus
import org.apache.hoya.api.ClusterDescription
import org.apache.hoya.api.RoleKeys
import org.apache.slider.providers.hbase.HBaseKeys
import org.apache.hoya.yarn.Arguments
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.slider.providers.hbase.minicluster.HBaseMiniClusterTestBase
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.junit.Test

/**
 * test create a live region service
 */
@CompileStatic
@Slf4j

class TestLiveTwoNodeRegionService extends HBaseMiniClusterTestBase {

  @Test
  public void testLiveTwoNodeRegionService() throws Throwable {
    
    String clustername = "test_live_two_node_regionservice"
    int regionServerCount = 2
    createMiniCluster(clustername, configuration, 1, 1, 1, true, false)

    describe(" Create a two node region service cluster");

    //now launch the cluster
    ServiceLauncher<HoyaClient> launcher = createHBaseCluster(clustername, regionServerCount,
        [
            Arguments.ARG_COMP_OPT,  HBaseKeys.ROLE_MASTER, RoleKeys.JVM_HEAP , HB_HEAP,
            Arguments.ARG_COMP_OPT,  HBaseKeys.ROLE_WORKER, RoleKeys.JVM_HEAP , HB_HEAP
        ],
        true,
        true)
    HoyaClient hoyaClient = launcher.service
    addToTeardown(hoyaClient);
    ClusterDescription status = hoyaClient.getClusterDescription(clustername)
    dumpClusterDescription("initial status", status)

    ClusterStatus clustat = basicHBaseClusterStartupSequence(hoyaClient)

    waitForWorkerInstanceCount(hoyaClient, regionServerCount, hbaseClusterStartupToLiveTime)
    //get the hbase status
    waitForHBaseRegionServerCount(hoyaClient, clustername, regionServerCount, hbaseClusterStartupToLiveTime)

    //now log the final status
    status = hoyaClient.getClusterDescription(clustername)

    dumpClusterDescription("final status", status)

    String rootPage = hoyaClient.applicationReport.originalTrackingUrl
    assert rootPage
    log.info("Slider root = ${rootPage}")
    def page = fetchWebPage(rootPage)
    log.info(page)
  }

}
