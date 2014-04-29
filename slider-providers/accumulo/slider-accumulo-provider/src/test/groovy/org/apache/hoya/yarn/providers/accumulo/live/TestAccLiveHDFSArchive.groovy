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

package org.apache.hoya.yarn.providers.accumulo.live

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.yarn.api.records.YarnApplicationState
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.apache.hoya.api.ClusterDescription
import org.apache.slider.providers.accumulo.AccumuloConfigFileOptions
import org.apache.slider.providers.accumulo.AccumuloKeys
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.hoya.yarn.providers.accumulo.AccumuloTestBase
import org.apache.slider.core.registry.zk.ZKIntegration
import org.junit.Test

@CompileStatic
@Slf4j
class TestAccLiveHDFSArchive extends AccumuloTestBase {

  @Test
  public void testAccLiveHDFSArchive() throws Throwable {
    String clustername = "test_acc_live_hdfs_archive"
    int tablets = 1
    int monitor = 1
    int gc = 1
    createMiniCluster(clustername, configuration, 1, 1, 1, true, true)
    describe(" Create an accumulo cluster from an archive");

    enableTestRunAgainstUploadedArchive();
    //make sure that ZK is up and running at the binding string
    ZKIntegration zki = createZKIntegrationInstance(ZKBinding, clustername, false, false, 5000)
    log.info("ZK up at $zki");
    //now launch the cluster
    Map<String, Integer> roles = [
        (AccumuloKeys.ROLE_MASTER): 1,
        (AccumuloKeys.ROLE_TABLET): tablets,
        (AccumuloKeys.ROLE_MONITOR): monitor,
        (AccumuloKeys.ROLE_GARBAGE_COLLECTOR): gc
    ];
    ServiceLauncher launcher = createAccCluster(clustername, roles, [], true, true)
    HoyaClient hoyaClient = launcher.service
    addToTeardown(hoyaClient);


    waitWhileClusterLive(hoyaClient);
    assert hoyaClient.applicationReport.yarnApplicationState == YarnApplicationState.RUNNING
    waitForRoleCount(hoyaClient, roles, ACCUMULO_CLUSTER_STARTUP_TO_LIVE_TIME)
    describe("Cluster status")
    ClusterDescription status
    status = hoyaClient.getClusterDescription(clustername)
    log.info(prettyPrint(status.toJsonString()))

    //now give the cluster a bit of time to actually start work

    log.info("Sleeping for a while")
    sleep(ACCUMULO_GO_LIVE_TIME);

    //verify that all is still there
    waitForRoleCount(hoyaClient, roles, 0)

    String page = fetchLocalPage(AccumuloConfigFileOptions.MONITOR_PORT_CLIENT_INT,
                                 AccumuloKeys.MONITOR_PAGE_JSON)
    log.info(page);

    log.info("Finishing")
    status = hoyaClient.getClusterDescription(clustername)
    log.info(prettyPrint(status.toJsonString()))
    maybeStopCluster(hoyaClient, clustername, "shut down $clustername")

  }

}
