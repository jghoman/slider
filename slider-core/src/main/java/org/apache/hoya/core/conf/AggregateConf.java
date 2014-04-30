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

package org.apache.hoya.core.conf;

import org.apache.slider.core.exceptions.BadConfigException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Aggregate Configuration.
 *
 * It is serializable to JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public final class AggregateConf {

  private String name;
  private ConfTree resources;
  private ConfTree internal;
  private ConfTree appConf;

  private ConfTreeOperations resourceOperations;
  private ConfTreeOperations appConfOperations;
  private ConfTreeOperations internalOperations;

  public AggregateConf() {
    this(new ConfTree(), new ConfTree(), new ConfTree());
  }

  public AggregateConf(String name) {
    this(new ConfTree(), new ConfTree(), new ConfTree());
    this.name = name;
  }

  public AggregateConf(ConfTree resources,
                       ConfTree appConf,
                       ConfTree internal) {
    setResources(resources);
    setAppConf(appConf);
    setInternal(internal);
  }

  public void setResources(ConfTree resources) {
    this.resources = resources;
    resourceOperations = new ConfTreeOperations(resources);
  }

  public void setAppConf(ConfTree appConf) {
    this.appConf = appConf;
    appConfOperations = new ConfTreeOperations(appConf);
  }

  public ConfTree getInternal() {
    return internal;
  }

  public void setInternal(ConfTree internal) {
    this.internal = internal;
    internalOperations = new ConfTreeOperations(internal);
  }

  public ConfTree getResources() {
    return resources;
  }

  public ConfTree getAppConf() {
    return appConf;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonIgnore
  public ConfTreeOperations getResourceOperations() {
    return resourceOperations;
  }


  @JsonIgnore
  public ConfTreeOperations getAppConfOperations() {
    return appConfOperations;
  }

  @JsonIgnore
  public ConfTreeOperations getInternalOperations() {
    return internalOperations;
  }

  /**
   * predicate to query if all sections have data structures
   * @return true if every section is non-null
   */
  @JsonIgnore
  public boolean isComplete() {
    return resources != null && appConf != null && internal != null;
  }

  public void validate() throws BadConfigException {
    if (!isComplete()) {
      throw new BadConfigException("Incomplete instance %s", this);
    }
    resourceOperations.validate();
    internalOperations.validate();
    appConfOperations.validate();
  }

  public void resolve() throws BadConfigException {
    validate();
    resourceOperations.resolve();
    internalOperations.resolve();
    appConfOperations.resolve();
  }

  /**
   * string operation includes all the inner conftrees
   * @return a string description
   */
  @Override
  public String toString() {
    final StringBuilder sb =
      new StringBuilder("{");
    sb.append(",\n\"internal\": ").append(internal);
    sb.append(",\n\"resources\": ").append(resources);
    sb.append(",\n\"appConf\" :").append(appConf);
    sb.append('}');
    return sb.toString();
  }
}
