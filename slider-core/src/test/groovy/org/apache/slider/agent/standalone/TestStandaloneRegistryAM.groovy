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

package org.apache.slider.agent.standalone

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.yarn.api.records.ApplicationReport
import org.apache.hadoop.yarn.api.records.YarnApplicationState
import org.apache.hadoop.yarn.service.launcher.ServiceLauncher
import org.apache.hoya.HoyaKeys
import org.apache.hoya.api.ClusterNode
import org.apache.hoya.yarn.client.HoyaClient
import org.apache.slider.agent.AgentMiniClusterTestBase
import org.apache.slider.core.registry.info.ServiceInstanceData
import org.apache.slider.server.services.curator.CuratorServiceInstance
import org.junit.Test

/**
 * create masterless AMs and work with them. This is faster than
 * bringing up full clusters
 */
@CompileStatic
@Slf4j

class TestStandaloneRegistryAM extends AgentMiniClusterTestBase {

  @Test
  public void testRegistryAM() throws Throwable {
    

    describe "create a masterless AM then perform registry operations on it"

    //launch fake master
    String clustername = "test_registry_am"
    createMiniCluster(clustername, configuration, 1, true)
    ServiceLauncher<HoyaClient> launcher
    launcher = createMasterlessAM(clustername, 0, true, false)
    HoyaClient client = launcher.service
    addToTeardown(client);

    ApplicationReport report = waitForClusterLive(client)
    logReport(report)
    List<ApplicationReport> apps = client.applications;

    List<ClusterNode> clusterNodes = client.listClusterNodesInRole(
        HoyaKeys.COMPONENT_AM)
    assert clusterNodes.size() == 1

    ClusterNode masterNode = clusterNodes[0]
    log.info("Master node = ${masterNode}");

    List<ClusterNode> nodes
    String[] uuids = client.listNodeUUIDsByRole(HoyaKeys.COMPONENT_AM)
    assert uuids.length == 1;
    nodes = client.listClusterNodes(uuids);
    assert nodes.size() == 1;
    describe "AM Node UUID=${uuids[0]}"

    nodes = listNodesInRole(client, HoyaKeys.COMPONENT_AM)
    assert nodes.size() == 1;
    nodes = listNodesInRole(client, "")
    assert nodes.size() == 1;
    ClusterNode master = nodes[0]
    assert master.role == HoyaKeys.COMPONENT_AM




    String username = client.username
    def serviceRegistryClient = client.YARNRegistryClient
    describe("list of all applications")
    logApplications(apps)
    describe("apps of user $username")
    List<ApplicationReport> userInstances = serviceRegistryClient.listInstances()
    logApplications(userInstances)
    assert userInstances.size() == 1
    describe("named app $clustername")
    ApplicationReport instance = serviceRegistryClient.findInstance(clustername)
    logReport(instance)
    assert instance != null

    //switch to the ZK-based registry

    def names = client.listRegistryNames(clustername)
    dumpRegistryNames(names)

    List<String> instanceIds = client.listRegistryInstanceIDs(clustername)


    dumpRegistryInstanceIDs(instanceIds)
    assert instanceIds.size() == 1

    List<CuratorServiceInstance<ServiceInstanceData>> instances = client.listRegistryInstances(
        clustername)
    dumpRegistryInstances(instances)

    assert instances.size() == 1

    def amInstance = instances[0]
    def serviceInstanceData = amInstance.payload

    def externalEndpoints = serviceInstanceData.externalView.endpoints


    // hit the registry web page

    def registryEndpoint = externalEndpoints.get("registry")
    def registry = registryEndpoint.asURL()
    describe("Registry WADL @ $registry")

    log.info(fetchWebPage(registry))
/*

    describe("Registry List")
    log.info(fetchWebPage(new URL(registry,"v1/service")))
*/


    def publisher = externalEndpoints.get("publisher").asURL()
    describe("Publisher")
    log.info(fetchWebPage(publisher))
//    log.info(fetchWebPage(new URL(publisher, "v1/service")))
    
    describe "teardown of cluster"
    //now kill that cluster
    assert 0 == clusterActionFreeze(client, clustername)
    //list it & See if it is still there
    ApplicationReport oldInstance = serviceRegistryClient.findInstance(
        clustername)
    assert oldInstance != null
    assert oldInstance.yarnApplicationState >= YarnApplicationState.FINISHED


    sleep(20000)

    instances = client.listRegistryInstances(clustername)
    assert instances.size() == 0

  }


}
