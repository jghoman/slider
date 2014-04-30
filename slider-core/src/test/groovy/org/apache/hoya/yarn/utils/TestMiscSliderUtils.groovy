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

package org.apache.hoya.yarn.utils

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem as HadoopFS
import org.apache.hadoop.fs.Path
import org.apache.slider.common.tools.SliderFileSystem
import org.apache.hoya.yarn.SliderTestBase
import org.junit.Test

class TestMiscSliderUtils extends SliderTestBase {


  public static final String CLUSTER1 = "cluster1"

  @Test
  public void testPurgeTempDir() throws Throwable {
    //SliderUtils. //

    Configuration configuration = new Configuration()
    HadoopFS fs = HadoopFS.get(new URI("file:///"), configuration)
    SliderFileSystem hoyaFileSystem = new SliderFileSystem(fs, configuration)
    Path inst = hoyaFileSystem.createHoyaAppInstanceTempPath(CLUSTER1, "001")

    assert fs.exists(inst)
    hoyaFileSystem.purgeHoyaAppInstanceTempFiles(CLUSTER1)
    assert !fs.exists(inst)
  }
}
