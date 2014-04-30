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

package org.apache.slider.funtest.commands

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.bigtop.itest.shell.Shell
import org.apache.slider.funtest.framework.CommandTestBase
import org.apache.slider.funtest.framework.SliderShell
import org.apache.slider.common.params.SliderActions
import org.junit.Test

@CompileStatic
@Slf4j
public class TestSimpleCommands extends CommandTestBase {

  @Test
  public void testVersion() throws Throwable {
    Shell shell = slider([SliderActions.ACTION_VERSION])
    assertSuccess(shell)
  }

  @Test
  public void testUsage() throws Throwable {
    SliderShell shell = slider(0, [SliderActions.ACTION_USAGE])
    assertSuccess(shell)
  }
  
}
