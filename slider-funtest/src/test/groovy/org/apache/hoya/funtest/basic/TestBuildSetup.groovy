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

package org.apache.hoya.funtest.basic

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hoya.funtest.abstracttests.AbstractTestBuildSetup
import org.apache.hoya.funtest.framework.FuntestProperties
import org.apache.hoya.testtools.HoyaTestUtils
import org.apache.hoya.tools.HoyaUtils
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
class TestBuildSetup extends AbstractTestBuildSetup {
}
