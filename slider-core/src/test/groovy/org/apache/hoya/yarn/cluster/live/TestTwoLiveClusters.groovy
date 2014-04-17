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

package org.apache.hoya.yarn.cluster.live

import groovy.util.logging.Slf4j
import org.apache.hoya.yarn.Arguments
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.hoya.yarn.providers.hbase.HBaseMiniClusterTestBase
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.apache.slider.core.registry.info.ServiceInstanceData
import org.apache.slider.server.services.curator.CuratorServiceInstance
import org.junit.Test

//@CompileStatic
@Slf4j

class TestTwoLiveClusters extends HBaseMiniClusterTestBase {

  /**
   * Create two clusters simultaneously and verify that their lifecycle is
   * independent.
   * @throws Throwable
   */
  @Test
  public void testTwoLiveClusters() throws Throwable {
    createMiniCluster("TestTwoLiveClusters", configuration, 1, true)
    String clustername1 = "testtwoliveclusters-a"
    //now launch the cluster
    int regionServerCount = 1
    ServiceLauncher launcher = createHBaseCluster(clustername1, regionServerCount, [], true, true) 
    HoyaClient hoyaClient = (HoyaClient) launcher.service
    addToTeardown(hoyaClient);

    basicHBaseClusterStartupSequence(hoyaClient)
    
    //verify the #of region servers is as expected
    dumpClusterStatus(hoyaClient, "post-hbase-boot status")

    //get the hbase status
    waitForHoyaWorkerCount(hoyaClient, 1, HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)
    waitForHBaseRegionServerCount(hoyaClient, clustername1, 1, HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)

    //now here comes cluster #2
    String clustername2 = "testtwoliveclusters-b"

    launcher = createHBaseCluster(clustername2, regionServerCount,
                                 [
                                     Arguments.ARG_ZKPATH, "/$clustername2",
                                 ],
                                 true,
                                 true)
    HoyaClient cluster2Client = launcher.service as HoyaClient
    addToTeardown(cluster2Client);

    basicHBaseClusterStartupSequence(cluster2Client)
    waitForHoyaWorkerCount(cluster2Client, 1, HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)
    waitForHBaseRegionServerCount(cluster2Client, clustername2, 1, HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)

    //and now verify that cluster 1 is still happy
    waitForHBaseRegionServerCount(hoyaClient, clustername1, 1, HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)

    // registry instances
    def names = hoyaClient.listRegistryNames(clustername1)
    dumpRegistryNames(names)

    List<String> instanceIds = hoyaClient.listRegistryInstanceIDs(clustername1)


    dumpRegistryInstanceIDs(instanceIds)
    assert names.size() == 1
    assert instanceIds.size() == 2


    List<CuratorServiceInstance<ServiceInstanceData>> instances = hoyaClient.listRegistryInstances(
        clustername1)
    dumpRegistryInstances(instances)
    assert instances.size() == 2

    clusterActionFreeze(cluster2Client, clustername2,"freeze cluster 2")
    clusterActionFreeze(hoyaClient, clustername1,"Freeze cluster 1")

  }

}
