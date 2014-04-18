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

package org.apache.hoya.providers.hbase.standalone.archives

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.hbase.ClusterStatus
import org.apache.hoya.api.ClusterDescription
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.hoya.providers.hbase.standalone.HBaseMiniClusterTestBase
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.junit.Test

/**
 * Test of RM creation. This is so the later test's prereq's can be met
 */
@CompileStatic
@Slf4j
class TestFreezeThawClusterFromArchive extends HBaseMiniClusterTestBase {


  @Test
  public void testFreezeThawClusterFromArchive() throws Throwable {
    String clustername = "test_freeze_thaw_cluster_from_archive"
    int regionServerCount = 1
    createMiniCluster(clustername, configuration, 1, true)
    switchToImageDeploy = true
    ServiceLauncher<HoyaClient> launcher = createHBaseCluster(clustername, regionServerCount, [], true, true)
    HoyaClient hoyaClient = launcher.service
    ClusterDescription status = hoyaClient.getClusterDescription(clustername)
    log.info("${status.toJsonString()}")

    ClusterStatus clustat = basicHBaseClusterStartupSequence(hoyaClient)

    waitForHBaseRegionServerCount(hoyaClient, clustername, regionServerCount,
                            HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)


    clusterActionFreeze(hoyaClient, clustername, "test freeze")
    describe("Restarting cluster")
    killAllRegionServers();

    //now let's start the cluster up again
    ServiceLauncher launcher2 = thawHoyaCluster(clustername, [], true);
    HoyaClient newCluster = launcher2.service
    basicHBaseClusterStartupSequence(newCluster)

    //get the hbase status
    waitForHBaseRegionServerCount(newCluster, clustername, regionServerCount,
                            HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)

  }




}
