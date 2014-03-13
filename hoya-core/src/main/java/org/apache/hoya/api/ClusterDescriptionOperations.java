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

package org.apache.hoya.api;

import org.apache.hoya.core.conf.AggregateConf;
import org.apache.hoya.core.conf.ConfTree;
import org.apache.hoya.core.conf.MapOperations;
import org.apache.hoya.exceptions.BadConfigException;
import org.apache.hoya.providers.HoyaProviderFactory;
import org.apache.hoya.tools.HoyaUtils;

import java.util.Map;

/**
 * Operations on Cluster Descriptions
 */
public class ClusterDescriptionOperations {


  public static ClusterDescription buildFromAggregateConf(AggregateConf aggregateConf) throws
                                                                                       BadConfigException {

    ClusterDescription cd = new ClusterDescription();
    
    aggregateConf.resolve();

    //options are a merge of all globals
    Map<String, String> options = cd.options;
    HoyaUtils.mergeMapsIgnoreDuplicateKeys(options,
                                           aggregateConf.getInternal().global);
    HoyaUtils.mergeMapsIgnoreDuplicateKeys(options,
                                           aggregateConf.getResources().global);
    HoyaUtils.mergeMapsIgnoreDuplicateKeys(options,
                                           aggregateConf.getAppConf().global);

    //roles are the role values merged in the same order
    mergeInComponentMap(cd, aggregateConf.getResources());
    mergeInComponentMap(cd, aggregateConf.getAppConf());

    //now add the extra bits
    cd.state = ClusterDescription.STATE_LIVE;
    MapOperations internalOptions =
      aggregateConf.getInternalOperations().getGlobalOptions();
    cd.type = internalOptions.getOption(OptionKeys.APPLICATION_TYPE,
                                HoyaProviderFactory.DEFAULT_CLUSTER_TYPE);
    
    return cd;
  }

  private static void mergeInComponentMap(ClusterDescription cd,
                                          ConfTree confTree
                                          ) {

    Map<String, Map<String, String>> components = confTree.components;
    for (Map.Entry<String, Map<String, String>> compEntry : components.entrySet()) {
      String name = compEntry.getKey();
      Map<String, String> role = cd.getOrAddRole(name);
      HoyaUtils.mergeMapsIgnoreDuplicateKeys(role, compEntry.getValue());
    }
  }
}