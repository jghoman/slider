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

package org.apache.hoya.yarn.model.appstate

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.slider.api.ResourceKeys
import org.apache.slider.server.avro.RoleHistoryWriter
import org.apache.slider.core.conf.ConfTreeOperations
import org.apache.slider.core.exceptions.BadConfigException
import org.apache.slider.server.appmaster.state.AppState
import org.apache.hoya.yarn.model.mock.BaseMockAppStateTest
import org.apache.hoya.yarn.model.mock.MockRecordFactory
import org.apache.hoya.yarn.model.mock.MockRoles
import org.apache.hoya.yarn.model.mock.MockYarnEngine
import org.junit.Test

/**
 * Test that if you have >1 role, the right roles are chosen for release.
 */
@CompileStatic
@Slf4j
class TestFlexDynamicRoles extends BaseMockAppStateTest
    implements MockRoles {

  @Override
  String getTestName() {
    return "TestAppStateDynamicRoles"
  }

  /**
   * Small cluster with multiple containers per node,
   * to guarantee many container allocations on each node
   * @return
   */
  @Override
  MockYarnEngine createYarnEngine() {
    return new MockYarnEngine(4, 4)
  }

  @Override
  void initApp() {
    super.initApp()
    appState = new AppState(new MockRecordFactory())
    appState.setContainerLimits(RM_MAX_RAM, RM_MAX_CORES)

    def instance = factory.newInstanceDefinition(0, 0, 0)

    def opts = [
        (ResourceKeys.COMPONENT_INSTANCES): "1",
        (ResourceKeys.COMPONENT_PRIORITY): "6",
    ]

    instance.resourceOperations.components["dynamic"] = opts

    
    appState.buildInstance(instance,
        new Configuration(false),
        factory.ROLES,
        fs,
        historyPath,
        null, null)
  }

  
  private ConfTreeOperations init() {
    createAndStartNodes();
    def resources = appState.instanceDefinition.resources;
    return new ConfTreeOperations(resources)
  }

  @Test
  public void testDynamicFlexAddRole() throws Throwable {
    def cd = init()
    def opts = [
        (ResourceKeys.COMPONENT_INSTANCES): "1",
        (ResourceKeys.COMPONENT_PRIORITY): "7",
    ]

    cd.components["role4"] = opts
    appState.updateResourceDefinitions(cd.confTree);
    createAndStartNodes();
    dumpClusterDescription("updated CD", appState.getClusterStatus())
    appState.lookupRoleStatus(7)
    appState.lookupRoleStatus(6)
    //gaps are still there
    try {
      assert null == appState.lookupRoleStatus(5)
    } catch (RuntimeException expected) {
    }
  }
  
  @Test
  public void testDynamicFlexAddRoleConflictingPriority() throws Throwable {
    def cd = init()
    def opts = [
        (ResourceKeys.COMPONENT_INSTANCES): "1",
        (ResourceKeys.COMPONENT_PRIORITY): "6",
    ]

    cd.components["role4"] = opts
    try {
      appState.updateResourceDefinitions(cd.confTree);
      dumpClusterDescription("updated CD", appState.getClusterStatus())
      fail("Expected an exception")
    } catch (BadConfigException expected) {
    }
  }
  
  @Test
  public void testDynamicFlexDropRole() throws Throwable {
    def cd = init()
    cd.components.remove("dynamic");
    appState.updateResourceDefinitions(cd.confTree);

    def getCD = appState.getClusterStatus()
    dumpClusterDescription("updated CD", getCD)
    //status is retained for future
    appState.lookupRoleStatus(6)
  }


  @Test
  public void testHistorySaveFlexLoad() throws Throwable {
    def cd = init()
    def roleHistory = appState.roleHistory
    Path history = roleHistory.saveHistory(0x0001)
    RoleHistoryWriter historyWriter = new RoleHistoryWriter();
    def opts = [
        (ResourceKeys.COMPONENT_INSTANCES): "1",
        (ResourceKeys.COMPONENT_PRIORITY): "7",
    ]

    cd.components["role4"] = opts
    appState.updateResourceDefinitions(cd.confTree);
    createAndStartNodes();
    historyWriter.read(fs, history, appState.roleHistory)
  }

  @Test
  public void testHistoryFlexSaveLoad() throws Throwable {
    def cd = init()
    def opts = [
        (ResourceKeys.COMPONENT_INSTANCES): "1",
        (ResourceKeys.COMPONENT_PRIORITY): "7",
    ]

    cd.components["role4"] = opts
    appState.updateResourceDefinitions(cd.confTree);
    createAndStartNodes();
    RoleHistoryWriter historyWriter = new RoleHistoryWriter();
    def roleHistory = appState.roleHistory
    Path history = roleHistory.saveHistory(0x0002)
    //now reset the app state
    def historyWorkDir2 = new File("target/history" + testName + "-0002")
    def historyPath2 = new Path(historyWorkDir2.toURI())
    appState = new AppState(new MockRecordFactory())
    appState.setContainerLimits(RM_MAX_RAM, RM_MAX_CORES)
    appState.buildInstance(
        factory.newInstanceDefinition(0, 0, 0),
        new Configuration(false),
        factory.ROLES,
        fs,
        historyPath2,
        null, null)
    historyWriter.read(fs, history, appState.roleHistory)
  }

}
