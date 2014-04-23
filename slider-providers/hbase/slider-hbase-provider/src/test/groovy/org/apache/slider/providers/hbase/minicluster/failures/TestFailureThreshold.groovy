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

package org.apache.slider.providers.hbase.minicluster.failures

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.hbase.ClusterStatus
import org.apache.hoya.HoyaExitCodes
import org.apache.hoya.api.ClusterDescription
import org.apache.hoya.api.OptionKeys
import org.apache.hoya.exceptions.BadClusterStateException
import org.apache.hoya.exceptions.ErrorStrings
import org.apache.hoya.yarn.Arguments
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.slider.providers.hbase.minicluster.HBaseMiniClusterTestBase
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.junit.Test

/**
 * test create a live region service
 */
@CompileStatic
@Slf4j
class TestFailureThreshold extends HBaseMiniClusterTestBase {

  @Test
  public void testFailedRegionService() throws Throwable {
    failureThresholdTestRun("test_failure_threshold", true, 2, 5)
  }


  private void failureThresholdTestRun(
      String testName,
      boolean toKill,
      int threshold,
      int killAttempts) {
    String clustername = testName
    String action = toKill ? "kill" : "stop"
    int regionServerCount = 2
    createMiniCluster(clustername, getConfiguration(), 1, 1, 1, true, true)
    describe(
        "Create a single region service cluster then " + action + " the RS");

    //now launch the cluster
    ServiceLauncher<HoyaClient> launcher = createHBaseCluster(
        clustername,
        regionServerCount,
        [
            Arguments.ARG_OPTION, OptionKeys.INTERNAL_CONTAINER_FAILURE_THRESHOLD,
            Integer.toString(threshold)],
        true,
        true)
    HoyaClient client = launcher.service
    addToTeardown(client);
    ClusterDescription status = client.getClusterDescription(clustername)

    ClusterStatus clustat = basicHBaseClusterStartupSequence(client)
    ClusterStatus hbaseStat
    try {
      for (i in 1..killAttempts) {
        status = waitForWorkerInstanceCount(
            client,
            regionServerCount,
            HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)
        //get the hbase status
/*
        hbaseStat = waitForHBaseRegionServerCount(
            client,
            clustername,
            regionServerCount,
            HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)

        log.info("Initial cluster status : ${hbaseStatusToString(hbaseStat)}");
*/
        describe("running processes")
        lsJavaProcesses()
        describe("about to " + action + " servers")
        if (toKill) {
          killAllRegionServers()
        } else {
          stopAllRegionServers()
        }

        //sleep a bit
        sleep(toKill ? 15000 : 25000);

        describe("waiting for recovery")

        //and expect a recovery
        status = waitForWorkerInstanceCount(
            client,
            regionServerCount,
            HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)
/*
        hbaseStat = waitForHBaseRegionServerCount(
            client,
            clustername,
            regionServerCount,
            HBASE_CLUSTER_STARTUP_TO_LIVE_TIME)*/
      }
    } catch (BadClusterStateException e) {
      assert e.toString().contains(ErrorStrings.E_APPLICATION_NOT_RUNNING)
      assert e.exitCode == HoyaExitCodes.EXIT_BAD_STATE
    }
    ApplicationReport report = client.applicationReport
    log.info(report.diagnostics)
    assert report.finalApplicationStatus == FinalApplicationStatus.FAILED
    assert report.diagnostics.contains(ErrorStrings.E_UNSTABLE_CLUSTER)

  }


}
