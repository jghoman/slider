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

package org.apache.hoya.funtest.abstracttests

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hoya.funtest.framework.FuntestProperties
import org.apache.hoya.testtools.HoyaTestUtils
import org.apache.hoya.tools.SliderUtils
import org.junit.Test

/**
 * Simple tests to verify that the build has been set up: if these
 * fail then the arguments to the test run are incomplete.
 *
 * This deliberately doesn't depend on HoyaCommandTestBase,
 * so that individual tests fail with more diagnostics
 * than the @BeforeClass failing
 */
@CompileStatic
@Slf4j
abstract class AbstractTestBuildSetup extends HoyaTestUtils implements FuntestProperties {


  String getRequiredSysprop(String name) {
    String val = System.getProperty(name)
    if (!val) {
      fail("System property not set : $name")

    }
    return val;
  }

  public String getHoyaConfDirProp() {
    return getRequiredSysprop(SLIDER_CONF_DIR_PROP)
  }

  public File getHoyaConfDirectory() {
    return getCanonicalFile(hoyaConfDirProp)
  }

  File getCanonicalFile(String dir) {
    assert dir
    return new File(dir).canonicalFile
  }

  File getBinDirectory() {
    return getCanonicalFile(getBinDirProp())
  }

  public String getBinDirProp() {
    return getRequiredSysprop(SLIDER_BIN_DIR_PROP)
  }

  public File getConfXML() {
    new File(hoyaConfDirectory, CLIENT_CONFIG_FILENAME).canonicalFile
  }

  public File getSliderScript() {
    new File(binDirectory, "bin/" + SCRIPT_NAME).canonicalFile
  }

  /**
   * Load the client XML file
   * @return
   */
  public Configuration loadHoyaConf() {
    Configuration conf = new Configuration(true)
    conf.addResource(confXML.toURI().toURL())
    return conf
  }

  @Test
  public void testConfDirSet() throws Throwable {
    assert getHoyaConfDirProp()
    log.info("Slider Configuration directory $hoyaConfDirProp")
  }

  @Test
  public void testConfDirExists() throws Throwable {
    assert hoyaConfDirectory.exists()
  }


  @Test
  public void testConfDirHasClientXML() throws Throwable {
    File clientXMLFile = confXML
    assert clientXMLFile.exists()
    clientXMLFile.toString()
  }


  @Test
  public void testBinDirExists() throws Throwable {
    log.info("binaries dir = $binDirectory")
  }

  @Test
  public void testBinScriptExists() throws Throwable {
    assert sliderScript.exists()
  }

  @Test
  public void testConfLoad() throws Throwable {
    Configuration conf = loadHoyaConf()
  }

  @Test
  public void testConfHasDefaultFS() throws Throwable {
    Configuration conf = loadHoyaConf()
    assumeBoolOption(conf, KEY_HOYA_FUNTESTS_ENABLED, true)
    String fs = conf.get("fs.defaultFS")
    log.info("Test Filesystem $fs")
    assert fs != null
  }


  @Test
  public void testConfHasRM() throws Throwable {

    Configuration conf = loadHoyaConf()
    assumeBoolOption(conf, KEY_HOYA_FUNTESTS_ENABLED, true)
    String val = conf.get(YarnConfiguration.RM_ADDRESS)
    log.info("$YarnConfiguration.RM_ADDRESS = $val")
    assert val != YarnConfiguration.DEFAULT_RM_ADDRESS
  }

  @Test
  public void testSecuritySettingsValid() throws Throwable {
    Configuration conf = loadHoyaConf();
    if (SliderUtils.maybeInitSecurity(conf)) {
      log.info("Security enabled")
      SliderUtils.forceLogin()
    }
    log.info("Login User = ${UserGroupInformation.getLoginUser()}")
  }

  @Test
  public void testHBaseBuildsHavePathsDefined() throws Throwable {
    Configuration conf = loadHoyaConf();
    assumeBoolOption(conf, KEY_HOYA_FUNTESTS_ENABLED, true)

    assumeBoolOption(conf, KEY_TEST_HBASE_ENABLED, true)

    assertStringOptionSet(conf, KEY_TEST_HBASE_APPCONF)
    assertStringOptionSet(conf, KEY_TEST_HBASE_TAR)
  }
  
  @Test
  public void testAccumuloBuildsHavePathsDefined() throws Throwable {
    Configuration conf = loadHoyaConf();
    assumeBoolOption(conf, KEY_HOYA_FUNTESTS_ENABLED, true)

    assumeBoolOption(conf, KEY_TEST_ACCUMULO_ENABLED, true)

    assertStringOptionSet(conf, KEY_TEST_ACCUMULO_APPCONF)
    assertStringOptionSet(conf, KEY_TEST_ACCUMULO_TAR)
  }
  
}
