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

package org.apache.hoya.yarn.utils

import groovy.transform.CompileStatic
import org.apache.slider.common.tools.SliderUtils
import org.junit.Assert
import org.junit.Test

@CompileStatic
class TestClusterNames {

  void assertValidName(String name) {
    boolean valid = SliderUtils.isClusternameValid(name)
    Assert.assertTrue("Clustername '$name' mistakenly declared invalid",
                      valid)
  }

  void assertInvalidName(String name) {
    boolean valid = SliderUtils.isClusternameValid(name)
    Assert.assertFalse("Clustername '$name' mistakenly declared valid",
                       valid)
  }

  void assertInvalid(List<String> names) {
    names.each { String name -> assertInvalidName(name) }
  }

  void assertValid(List<String> names) {
    names.each { String name -> assertValidName(name) }
  }

  @Test
  public void testEmptyName() throws Throwable {
    assertInvalidName('')
  }

  @Test
  public void testSpaceName() throws Throwable {
    assertInvalidName(' ')
  }


  @Test
  public void testLeadingHyphen() throws Throwable {
    assertInvalidName('-hyphen')
  }
  
  @Test
  public void testTitleLetters() throws Throwable {
    assertInvalidName('Title')
  }
    
  @Test
  public void testCapitalLetters() throws Throwable {
    assertInvalidName('UPPER-CASE-CLUSTER')
  }
    
  @Test
  public void testInnerBraced() throws Throwable {
    assertInvalidName('a[a')
  }
  
  @Test
  public void testLeadingBrace() throws Throwable {
    assertInvalidName('[')
  }

  @Test
  public void testNonalphaLeadingChars() throws Throwable {
    assertInvalid([
        '[a', '#', '@', '=', '*', '.'
    ])
  }

  @Test
  public void testNonalphaInnerChars() throws Throwable {
    assertInvalid([
        'a[a', 'b#', 'c@', 'd=', 'e*', 'f.', 'g ', 'h i'
    ])
  }

  @Test
  public void testClusterValid() throws Throwable {
    assertValidName('cluster')
  }

  @Test
  public void testValidNames() throws Throwable {
    assertValid([
        'cluster',
        'cluster1',
        'very-very-very-long-cluster-name',
        'c1234567890'
    ])


  }


}
